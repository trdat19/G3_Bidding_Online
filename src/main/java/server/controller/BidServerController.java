package server.controller;

import server.network.ClientConnectionHandler;
import server.service.BidService;
import shared.request.BaseRequest;
import shared.response.BaseResponse;

public class BidServerController {
    // Khởi tạo Service để xử lý logic "hardcore"
    private final BidService bidService = new BidService();

    // Sửa hàm placeBid để nhận dữ liệu từ Handler
    public BaseResponse placeBid(BaseRequest request, ClientConnectionHandler handler) {
        // 1. Giả sử request.getData() là một Object/Map chứa {auctionId, userId, amount}
        // Bạn bóc tách dữ liệu ở đây (ép kiểu tùy theo nhóm bạn quy định)
        int auctionId = 1; // Ví dụ
        String userId = "User01";
        double amount = 500.0;

        // 2. Gọi Service xử lý logic và kích hoạt Realtime Push
        bidService.placeBid(auctionId, userId, amount);

        // 3. Trả về phản hồi cho chính người vừa đặt giá
        return new BaseResponse(true, "Yêu cầu đặt giá của bạn đang được xử lý!", null);
    }

    public BaseResponse getBidHistory(BaseRequest request) {
        // Gọi Service lấy dữ liệu từ Database (hoặc List lịch sử)
        bidService.getHistory(1);
        return new BaseResponse(true, "Lấy lịch sử thành công", null);
    }
}
