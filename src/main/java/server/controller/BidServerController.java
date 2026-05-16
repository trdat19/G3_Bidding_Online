package server.controller;

import server.network.ClientConnectionHandler;
import server.service.BidService;
import shared.dto.request.BaseRequest;
import shared.dto.request.bid.PlaceBidRequest;
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
        PlaceBidRequest bidRequest = (PlaceBidRequest) request;

        long auctionId = bidRequest.getAuctionId();
        String userId = handler.getUser().getUsername();
        double amount = bidRequest.getAmount();

        bidService.placeBid(auctionId, userId, amount);

        return new BaseResponse(true, "Yêu cầu đặt giá của bạn đang được xử lý!");
    }

    public BaseResponse getBidHistory(BaseRequest request) {
        // Gọi Service lấy dữ liệu từ Database (hoặc List lịch sử)
//        bidService.getHistory(1);
        return new BaseResponse(true, "Lấy lịch sử thành công");
    }
}
