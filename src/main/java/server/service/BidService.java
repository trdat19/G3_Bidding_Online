package server.service;

import server.network.RealtimePushServer;
import shared.dto.response.BaseResponse;

import java.util.HashMap;
import java.util.Map;

public class BidService {
    // Sử dụng Singleton để đảm bảo mọi luồng đều dùng chung một đối tượng xử lý giá
    private static BidService instance;

    public static synchronized BidService getInstance() {
        if (instance == null)
        {
            instance = new BidService();
        }

        return instance;
    }

    // Đặt giá
    public synchronized void placeBid(Long auctionId, String userId, double bidAmount) {
        // Kiểm tra xem giá có hợp lệ không
        if (validateBid(auctionId, bidAmount)) {

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

    public boolean validateBid(Long auctionId, double bidAmount) {
        double currentMax = currentPrices.getOrDefault(auctionId, 0.0);
        return bidAmount > currentMax; // Chỉ cho phép đặt nếu giá mới cao hơn giá cũ
    }

    public void updateLeader(Long auctionId, String userId, double bidAmount) {
        currentPrices.put(auctionId, bidAmount); // Cập nhật giá mới vào "Database ảo"
    }
}
