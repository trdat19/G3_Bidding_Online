package server.controller;

import server.network.ClientConnectionHandler;
import server.service.BidService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.util.Map;

public class BidServerController {
    private static BidServerController instance;
    public static synchronized BidServerController getInstance() {
        if (instance == null)
        {
            instance = new BidServerController();
        }
        return instance;
    }

    private final BidService bidService = BidService.getInstance();

    // THÊM DÒNG NÀY: Khóa cửa lại không cho ai 'new' từ bên ngoài
    private BidServerController() {}

    public BaseResponse placeBid(BaseRequest request, ClientConnectionHandler handler) {
        // 1. Giả sử request.getData() là một Object/Map chứa {auctionId, userId, amount}
        // Bạn bóc tách dữ liệu ở đây (ép kiểu tùy theo nhóm bạn quy định)
//        // Ví dụ
//        int auctionId = 1;
//        String userId = "User01";
//        double amount = 500.0;
        Map<String, Object> data = (Map<String, Object>) request.getData();
        int auctionId = Integer.parseInt(data.get("auctionId").toString());
        String userId = data.get("userId").toString();
        double amount = Double.parseDouble(data.get("amount").toString());

        // 2. Gọi Service xử lý logic và kích hoạt Realtime Push
        bidService.placeBid(auctionId, userId, amount);

        // 3. Trả về phản hồi cho chính người vừa đặt giá
        return new BaseResponse(true, "Yêu cầu đặt giá của bạn đang được xử lý!", null);
    }

    public BaseResponse getBidHistory(BaseRequest request) {
        // Gọi Service lấy dữ liệu từ Database (hoặc List lịch sử)
//        bidService.getHistory(1);
        return new BaseResponse(true, "Lấy lịch sử thành công", null);
    }
}
