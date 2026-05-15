package server.service;

import server.dao.AuctionDAO;
import server.model.core.Auction;
import server.model.core.Bid;
import server.network.RealtimePushServer;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Xử lý logic đặt giá, kết nối vơi DB
 * Singleton
 * Push realtime
 */
public class BidService {
    // Sử dụng Singleton để đảm bảo mọi luồng đều dùng chung một đối tượng xử lý giá
    private static BidService instance;

    private AuctionService auctionService = AuctionService.getInstance();
    private AuctionDAO auctionDAO = new AuctionDAO();

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
    public synchronized BaseResponse placeBid(Long auctionId, Long bidderId, BigDecimal amount) {

        // 1. Lấy phiên đấu giá từ DB
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            return new BaseResponse(false, "Phiên đấu giá không tồn tại!", null);
        }

        // 2. Kiểm tra trạng thái của phiên xem có đang mở không
        AuctionStatus status = auction.getStatus();
        if (status != AuctionStatus.OPEN && status != AuctionStatus.RUNNING) {
            return new BaseResponse(false, "Phiên đấu giá đã đóng!", null);
        }

        // 3. Kiểm tra thời gian
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            auctionService.finishAuction(auctionId); // Kết thúc phiên nếu chưa kịp đóng!
            return new BaseResponse(false, "Phiên đấu giá đã kết thúc!", null);
        }

        // 4. Tính mức giá tối thiểu hợp lệ

        // 5. Kiểm tra Buy-Now: Nếu bid >= buyNow -> chốt ngay

        // 6. Tạo và lưu Bid vào DB

        // 7.
        // Kiểm tra xem giá có hợp lệ không
        if (validateBid(auctionId, amount)) {

            // Cập nhật người dẫn đầu trong Database (logic DB để sau)
            updateLeader(auctionId, userId, bidAmount);

            // BÁO CHO TẤT CẢ MỌI NGƯỜI (Realtime)
            // Tạo một gói tin thông báo giá mới
            BaseResponse bidEvent = new BaseResponse(true, "NEW_BID_UPDATE", "Người dùng " + userId + " vừa đặt giá: " + bidAmount);

            // Gọi cái loa của bạn đây
            RealtimePushServer.pushToAuctionSubscribers(auctionId, bidEvent);

            System.out.println(">>> Đã đẩy thông báo giá mới cho phiên #" + auctionId);
        }
    }
    // 2. Kiểm tra giá: Giá mới phải cao hơn giá hiện tại
//    public boolean validateBid(int auctionId, double bidAmount) {
//        // Logic: Truy vấn DB lấy giá cao nhất hiện tại của auctionId
//        // double currentMax = database.getMaxBid(auctionId);
//        // return bidAmount > currentMax;
//        return true; // Tạm thời để true để test do chưa c DB
//    }
//
//    // 3. Cập nhật người dẫn đầu
//    public void updateLeader(int auctionId, String userId, double bidAmount) {
//        // Logic: Ghi vào bảng Auctions hoặc Bids trong Database
//        System.out.println(">>> Đang cập nhật " + userId + " làm leader phiên #" + auctionId);
//    }
//
//    // 4. Lấy lịch sử đấu giá
//    public void getHistory(int auctionId) {
//        // Logic: Trả về danh sách các lượt đặt giá của phiên này
//    }



// Tạm thời cưa có DataBase
    private static final Map<Long, Double> currentPrices = new HashMap<>(); // Lưu giá cao nhất tạm thời

    public boolean validateBid(Long auctionId, BigDecimal amount) {
        double currentMax = currentPrices.getOrDefault(auctionId, 0.0);
        return bidAmount > currentMax; // Chỉ cho phép đặt nếu giá mới cao hơn giá cũ
    }

    public void updateLeader(Long auctionId, String userId, double bidAmount) {
        currentPrices.put(auctionId, bidAmount); // Cập nhật giá mới vào "Database ảo"
    }
}
