package server.network;

import server.controller.AuthServerController;
import server.controller.BidServerController;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

public class RequestRouter {

    public static BaseResponse route(BaseRequest request, ClientConnectionHandler handler) {
        // 1. Kiểm tra request null để tránh Crash Server
        if (request == null || request.getAction() == null) {
            return new BaseResponse(false, "Yêu cầu không hợp lệ (Null Request)", null);
        }

        String action = request.getAction();
        Object data = request.getData();

        System.out.println(">>> [Router] Đang điều phối hành động: " + action);

        try {
            switch (action) {
                case "LOGIN":
                    return AuthServerController.getInstance().login(request, handler);

                case "REGISTER":

                    return AuthServerController.getInstance().register(request);

                case "PLACE_BID":
                    // 2. Kiểm tra quyền: Chỉ người đã login mới được đặt Bid
                    if (handler.getUser() == null) {
                        return new BaseResponse(false, "Bạn cần đăng nhập để thực hiện đặt giá!", null);
                    }
                    return BidServerController.getInstance().placeBid(request, handler);

                case "SUBSCRIBE_AUCTION":
                    // 3. Ép kiểu an toàn để tránh NumberFormatException
                    if (data == null) {
                        return new BaseResponse(false, "Thiếu ID phiên đấu giá", null);
                    }
                    try {
                        int auctionId = Integer.parseInt(data.toString());
                        RealtimePushServer.subscribeToAuction(auctionId, handler);
                        return new BaseResponse(true, "Đã tham gia phòng đấu giá #" + auctionId, null);
                    } catch (NumberFormatException e) {
                        return new BaseResponse(false, "ID phiên đấu giá phải là số nguyên", null);
                    }

                default:
                    return new BaseResponse(false, "Hành động '" + action + "' không tồn tại trên hệ thống", null);
            }
        } catch (Exception e) {
           e.printStackTrace();
            return new BaseResponse(false, "Lỗi Server: " + e.getMessage(), null);
        }
    }
}