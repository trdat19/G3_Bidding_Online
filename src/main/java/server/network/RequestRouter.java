package server.network;

import server.controller.AuthServerController;
import server.controller.BidServerController;
import server.controller.AuctionServerController;
import shared.request.BaseRequest;
import shared.response.BaseResponse;

public class RequestRouter {

    // Hàm điều hướng chính
    // Khởi tạo sẵn các bộ phận chuyên môn
    private static final AuthServerController authController = new AuthServerController();
    private static final BidServerController bidController = new BidServerController();
    public static BaseResponse route(BaseRequest request) {
        String action = request.getAction();
        Object data = request.getData();

        System.out.println(">>> Đang điều phối hành động: " + action);

        switch (action)
        {
            case "LOGIN":
                // Sau này sẽ gọi AuthServerController.login(data)
                return new BaseResponse(true, "Đăng nhập thành công (giả lập)", null);

            case "PLACE_BID":
                // Sau này sẽ gọi BidServerController.placeBid(data)
                return new BaseResponse(true, "Đặt giá thành công", null);

            default:
                return new BaseResponse(false, "Hành động không xác định", null);
        }
    }
}