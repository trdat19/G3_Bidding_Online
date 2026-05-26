package server.controller;

import server.network.ClientConnectionHandler;
import server.service.AutoBidService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.math.BigDecimal;
import java.util.Map;

public class AutoBidController {
    private static AutoBidController instance;

    private final AutoBidService autoBidService = AutoBidService.getInstance();

    private AutoBidController() {}

    public static AutoBidController getInstance() {
        if (instance == null) {
            synchronized (AutoBidController.class) {
                if (instance == null) {
                    instance = new AutoBidController();
                }
            }
        }
        return instance;
    }

    //---------REGISTER--------------
    public BaseResponse registerRule(BaseRequest request, ClientConnectionHandler handler) {
        try {
            // 1. Lấy id bidder từ session
            Long bidderId = handler.getUser().getId();

            // Bóc dữ liệu
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long auctionId = Long.parseLong(data.get("auctionId").toString());
            BigDecimal maxAmount = new BigDecimal(data.get("maxAmount").toString());
            BigDecimal stepAmount = new BigDecimal(data.get("stepAmount").toString());

            boolean registerCheck = autoBidService.registerAutoBidRule(auctionId, bidderId, maxAmount, stepAmount);
            if (registerCheck) {
                return new BaseResponse(true, "Đăng kí AutoBid thành công!", null);
            }

            return new BaseResponse(false, "Đăng kí AutoBid thất bại!", null);
        }
        catch (Exception e) {
            return new BaseResponse(false, "Có lỗi xảy ra: " + e.getMessage(), null);
        }
    }

    //----------------REMOVE---------------
//    public BaseResponse switchRule(BaseRequest request, ClientConnectionHandler handler) {
//
//    }
    public BaseResponse removeRule(BaseRequest request, ClientConnectionHandler handler) {
        try {
            Long bidderId = handler.getUser().getId();
            Long auctionId = Long.parseLong(request.getData().toString());

            boolean ok = autoBidService.switchAutoBid(auctionId, bidderId, false);

            if (ok) {
                return new BaseResponse(true, "Đã tắt Auto Bid!", null);
            }

            return new BaseResponse(false, "Không tìm thấy Rule!", null);
        }
        catch (Exception e) {
            return new BaseResponse(false, "Có lỗi xảy ra: " + e.getMessage(), null);
        }
    }
}