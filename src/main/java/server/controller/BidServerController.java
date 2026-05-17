package server.controller;

import server.network.ClientConnectionHandler;
import server.service.BidService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.exception.AuctionClosedException;
import shared.exception.AuctionNotFoundException;
import shared.exception.BidTooLowException;
import shared.exception.InvalidAuctionTimeException;

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
        try {
            // Lấy bidderId từ session
            Long bidderId = handler.getUser().getId();

            // 1. request.getData() là một Object/Map chứa {auctionId, bidderId, amount}
            // Bóc tách dữ liệu
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long auctionId = Long.parseLong(data.get("auctionId").toString());
            BigDecimal amount = new BigDecimal(data.get("amount").toString());

            boolean ok = bidService.placeBid(auctionId, bidderId, amount);
            if (!ok) {
                return new BaseResponse(false, "Đặt giá thất bại! Vui lòng thử lại sau.", null);
            }

            return new BaseResponse(true, "Yêu cầu đặt giá của bạn đã được xử lý!", null);
        }
        catch (AuctionNotFoundException e) {
            return new BaseResponse(false, e.getMessage(), null);
        }
        catch (AuctionClosedException e) {
            return new BaseResponse(false, e.getMessage(), null);
        }
        catch (BidTooLowException e) {
            return new BaseResponse(false, e.getMessage(), null);
        }
        catch (InvalidAuctionTimeException e) {
            return new BaseResponse(false, e.getMessage(), null);
        }
        catch (NumberFormatException e) {
            return new BaseResponse(false, "Số tiền đặt giá không hợp lệ!", null);
        }
        catch (Exception e) {
            //e.printStackTrace();
            return new BaseResponse(false, "Lỗi hệ thống: " + e.getMessage(), null);
        }
    }


}
