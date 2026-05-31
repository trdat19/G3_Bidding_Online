package server.service;

import server.concurrency.AuctionLockManager;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;
import server.model.core.Auction;
import server.model.core.Bid;
import server.model.user.User;
import server.network.RealtimePushServer;

import shared.dto.common.BidDTO;
import shared.dto.response.BaseResponse;
import shared.enums.AuctionStatus;
import shared.exception.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/** Xử lý logic đặt giá, kết nối vơi DB
 * Singleton
 * Push realtime
 */
public class BidService {

    //anti-sniping
    private static final int ANTI_SNIPE_THRESHOLD_SECONDS = 60; // bid trong 60s cuối
    private static final int ANTI_SNIPE_EXTENSION_SECONDS = 60; // gia hạn thêm 60s

    // Sử dụng Singleton để đảm bảo mọi luồng đều dùng chung một đối tượng xử lý giá
    private static volatile BidService instance;

    private final AuctionService auctionService = AuctionService.getInstance();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidDAO bidDAO = new BidDAO();
    private final UserDAO userDAO =  new UserDAO();

    private BidService() {}

    //double-checked locking
    public static BidService getInstance() {
        if (instance == null) {
            synchronized (BidService.class) {
                if (instance == null) {
                    instance = new BidService();
                }
            }
        }
        return instance;
    }

    /** Thao tác ghi Bid vào trong DB */
    private boolean placeBidInternal(Long auctionId, Long bidderId, BigDecimal amount, boolean isAutoBid) {

        // 1. Lấy phiên đấu giá từ DB -> validate
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException(auctionId);
        }

        // 2. Kiểm tra thời gian
        LocalDateTime now = LocalDateTime.now();
        if (auction.getStartTime() != null && now.isBefore(auction.getStartTime())) {
            throw new InvalidAuctionTimeException(now);
        }
        if (auction.getEndTime() != null && now.isAfter(auction.getEndTime())) {
            auctionService.finishAuction(auctionId); // Đóng phiên nếu chưa kịp đóng.
            throw new InvalidAuctionTimeException(now);
        }

        // 4. Chỉ cho đặt giá nếu đang RUNNING, OPEN
        AuctionStatus status = auction.getStatus();
        if (status != AuctionStatus.OPEN && status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException();
        }

        // 5. Đổi trạng thái nếu cần (Nếu là bid đầu tiên)
        if (auction.getStatus() == AuctionStatus.OPEN) {
            auctionDAO.updateStatus(auctionId, AuctionStatus.RUNNING);
            auction.setStatus(AuctionStatus.RUNNING);
        }

        // 6. Tính mức giá tối thiểu hợp lệ
        Bid currentHighest = bidDAO.getHighestBidByAuctionId(auctionId);
        BigDecimal minIncrement = auction.getMinIncrement() != null ? auction.getMinIncrement() : BigDecimal.ZERO;

        BigDecimal minBid;
        if (currentHighest == null) {
            minBid = auction.getStartPrice().add(minIncrement);
        }
        else {
            minBid = currentHighest.getAmount().add(minIncrement);
        }

        // 7. Kiểm tra giá hợp lệ
        if (amount.compareTo(minBid) < 0) {
            throw new BidTooLowException(minBid);
        }

        // 8. Kiểm tra ví
        try {
            WalletService.getInstance().checkCanBid(bidderId, auctionId, amount);
        } catch (InsufficientBalanceException e) {
            return false;
        }

        // 9. Kiểm tra Buy-Now: Nếu bid >= buyNow -> chốt ngay
        boolean buyNowTriggered = auction.getBuyNowPrice() != null
                && amount.compareTo(auction.getBuyNowPrice()) >= 0;

        // 10. Tạo và lưu Bid vào DB
        Bid bid = new Bid(auctionId, bidderId, amount);
        bid.setTimestamp(LocalDateTime.now());
        bid.setIsAutoBid(isAutoBid);

        boolean saved = bidDAO.insertBid(bid);
        if (!saved) {
            return false;
        }

        // 11. Cập nhật max_price trong bảng
        auctionDAO.updateMaxPrice(auctionId, amount);

        // 12. Anti-sniping
        if (auction.getEndTime() != null) {
            Duration timeLeft = Duration.between(LocalDateTime.now(), auction.getEndTime());

            if (timeLeft.getSeconds() <= ANTI_SNIPE_THRESHOLD_SECONDS) {
                auctionService.extendAuction(auctionId, ANTI_SNIPE_EXTENSION_SECONDS);
            }
        }

        // 13. Buy Now
        if (buyNowTriggered) {
            auctionService.finishAuction(auctionId);
        }

        // 14. Lấy tên người đặt để hiển thị
        User bidder = userDAO.findById(bidderId);
        String bidderName = (bidder != null) ? bidder.getFullName() : String.format("Người dùng #%d", bidderId);

        // 15. Push realtime tới tất cả client đang xem phiên này
        BidDTO bidDTO = new BidDTO(bid.getId(), auctionId, bidderId,
                bidderName, amount, bid.getTimestamp());

        BaseResponse bidEvent = new BaseResponse(true, "NEW_BID", bidDTO);
        bidEvent.setAction("NEW_BID");

        RealtimePushServer.pushToAuctionSubscribers(auctionId, bidEvent);

        BaseResponse listEvent = new BaseResponse(
                true,
                "Danh sách phiên đấu giá đã thay đổi!",
                null
        );
        listEvent.setAction("AUCTION_LIST_CHANGED");
        RealtimePushServer.pushToAuctionListSubscribers(listEvent);

        System.out.printf(
                ">>> [BidService] %s %s giá %s cho phiên #%d%n",
                bidderName,
                isAutoBid ? "auto bid" : "đặt",
                amount,
                auctionId
        );
        return true;
    }

    /** Xử lí đặt giá tay, cấp khoá riêng ReentrantLock tránh lost update khi nhiều người đặt cùng lúc */
    public boolean placeBid(Long auctionId, Long bidderId, BigDecimal amount) {
        ReentrantLock lock = AuctionLockManager.getInstance().getLock(auctionId);
        lock.lock(); // lấy lock
        try {
            boolean ok = placeBidInternal(auctionId, bidderId, amount, false);

            if (!ok) {
                return false;
            }

            // Sau khi có bid tay mới, cho các AutoBid rule đang bật của người khác phản ứng
            Auction auction = auctionDAO.findById(auctionId);
            boolean buyNowTriggered = auction != null
                    && auction.getBuyNowPrice() != null
                    && amount.compareTo(auction.getBuyNowPrice()) >= 0;

            if (!buyNowTriggered) {
                // Có bid mới → kiểm tra các rule AutoBid đang bật của người khác
                AutoBidService.getInstance().reactToIncomingBid(auctionId);
            }

            return true;
        }
        finally {
            lock.unlock();
        }
    }

    public boolean placeAutoBid(Long auctionId, Long bidderId, BigDecimal amount) {
        ReentrantLock lock = AuctionLockManager.getInstance().getLock(auctionId);
        lock.lock();

        try {
            return placeBidInternal(auctionId, bidderId, amount, true);
        } finally {
            lock.unlock();
        }
    }
    /**
     * Validate không kết nối DB dùng để test
     */
    public boolean isAmountValid(BigDecimal amount, BigDecimal minBid) {
        return amount != null && minBid != null && amount.compareTo(minBid) >= 0;
    }
}
