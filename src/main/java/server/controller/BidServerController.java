package server.controller;

import server.model.core.Bid;
import server.network.ClientConnectionHandler;
import server.service.BidService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.math.BigDecimal;
import java.util.Map;

public class BidServerController {
    private static BidServerController instance;
    private final BidService bidService = BidService.getInstance();

    private BidServerController() {}

    //double-checked locking
    public static BidServerController getInstance() {
        if (instance == null) {

            synchronized (BidServerController.class) {
                if (instance == null) {
                    instance = new BidServerController();
                }
            }
        }
        return instance;
    }

    /** Xử lý đặt giá từ client, sau đó gọi Service để thực hiện và kích hoạt RealtimePush */
    public BaseResponse placeBid(BaseRequest request, ClientConnectionHandler handler) {

        // 1. Giả sử request.getData() là một Object/Map chứa {auctionId, bidderId, amount}
        // Bạn bóc tách dữ liệu ở đây (ép kiểu tùy theo nhóm bạn quy định)
//        // Ví dụ
//        Long auctionId = 1L;
//        Long bidderId = 1L;
//        BigDecimal amount = 500.0;

        Map<String, Object> data = (Map<String, Object>) request.getData();
        Long auctionId = Long.parseLong(data.get("auctionId").toString());
        Long bidderId = Long.parseLong(data.get("bidderId").toString());
        BigDecimal amount = new BigDecimal(data.get("amount").toString());

        // 2. Gọi Service xử lý logic và kích hoạt Realtime Push
        bidService.placeBid(auctionId, bidderId, amount);

        // 3. Trả về phản hồi cho chính người vừa đặt giá
        return new BaseResponse(true, "Yêu cầu đặt giá của bạn đang được xử lý!", null);
    }

    /** GET_BID_HISTORY */
    public BaseResponse getBidHistory(BaseRequest request) {
        try {
            Long auctionId = Long.parseLong(request.getData().toString());
            return auctionService.getBidHistory(auctionId);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi lịch sử bid: " + e.getMessage(), null);
        }
    }
}
