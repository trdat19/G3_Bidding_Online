package server.service;

import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;
import server.model.core.Auction;
import server.model.core.Bid;
import server.model.user.User;
import server.network.RealtimePushServer;
import shared.dto.common.BidDTO;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.AuctionStatus;
import shared.exception.AuctionClosedException;
import shared.exception.AuctionNotFoundException;
import shared.exception.BidTooLowException;
import shared.exception.InvalidAuctionTimeException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Xử lý logic đặt giá, kết nối vơi DB
 * Singleton
 * Push realtime
 */
public class BidService {

    private static final int ANTI_SNIPE_THRESHOLD_SECONDS = 60; // bid trong 60s cuối
    private static final int ANTI_SNIPE_EXTENSION_SECONDS = 60; // gia hạn thêm 60s

    // Sử dụng Singleton để đảm bảo mọi luồng đều dùng chung một đối tượng xử lý giá
    private static BidService instance;

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

    /**
     * Xử lí đặt giá chính, synchronized tránh lost update khi nhiều người đặt cùng lúc
     *
     * @param auctionId
     * @param bidderId
     * @param amount
     */
    public synchronized boolean placeBid(Long auctionId, Long bidderId, BigDecimal amount) {

        // 1. Lấy phiên đấu giá từ DB
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException(auctionId);
        }

        // 2. Kiểm tra trạng thái của phiên xem có đang mở không
        AuctionStatus status = auction.getStatus();
        if (status != AuctionStatus.OPEN && status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException();
        }

        // 3. Kiểm tra thời gian
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            auctionService.finishAuction(auctionId); // Kết thúc phiên nếu chưa kịp đóng!
            throw new InvalidAuctionTimeException(LocalDateTime.now());
        }

        // 4. Tính mức giá tối thiểu hợp lệ
        Bid currentHighest = bidDAO.getHighestBidByAuctionId(auctionId);
        BigDecimal minBid;
        if (currentHighest == null) {
            minBid = auction.getStartPrice();
        }
        else {
            minBid = currentHighest.getAmount().add(auction.getMinIncrement());
        }

        if (amount.compareTo(minBid) < 0) {
            throw new BidTooLowException(minBid);
        }

        // 5. Kiểm tra Buy-Now: Nếu bid >= buyNow -> chốt ngay
        boolean buyNowTriggered = auction.getBuyNowPrice() != null
                && amount.compareTo(auction.getBuyNowPrice()) >= 0;

        // 6. Tạo và lưu Bid vào DB
        Bid bid = new Bid(auctionId, bidderId, amount);
        bid.setTimestamp(LocalDateTime.now());;

        boolean saved = bidDAO.insertBid(bid);
        if (!saved) {
            return false;
        }

        // 7. Cập nhật max_price trong bảng
        auctionDAO.updateMaxPrice(auctionId, amount);

        // 8. Chuyển sang RUNNING nếu đây là bid đầu tiên
        if (currentHighest == null) {
            auction.setStatus(AuctionStatus.RUNNING);
            auctionDAO.updateStatus(auctionId, AuctionStatus.RUNNING);
        }

        // 9. Tự động gia hạn trong 60 cuối, anti-sniping
        Duration timeLeft = Duration.between(LocalDateTime.now(), auction.getEndTime());
        if (timeLeft.getSeconds() <= ANTI_SNIPE_THRESHOLD_SECONDS) {
            auctionService.extendAuction(auctionId, ANTI_SNIPE_EXTENSION_SECONDS);
        }

        // 10. Buy Now
        if (buyNowTriggered) {
            auctionService.finishAuction(auctionId);
        }

        // 11. Lấy tên người đặt để hiển thị
        User bidder = userDAO.findById(bidderId);
        String bidderName = (bidder != null) ? bidder.getFullName() : String.format("Người dùng #%d", bidderId);

        // 12. Push realtime tới tất cả client đang xem phiên này
        BidDTO bidDTO = new BidDTO(bid.getId(), auctionId, bidderId,
                bidderName, amount, bid.getTimestamp());
        BaseResponse bidEvent = new BaseResponse(true, "NEW_BID", bidDTO);
        RealtimePushServer.pushToAuctionSubscribers(auctionId, bidEvent);

        System.out.printf(">>> [BidService] %s đặt giá %s cho phiên #%d%n",
                bidderName, amount, auctionId);

        return true;
    }

    /**
     * Validate không kết nối DB dùng để test
     */
    public boolean isAmountValid(BigDecimal amount, BigDecimal minBid) {
        return amount != null && minBid != null && amount.compareTo(minBid) >= 0;
    }
}
