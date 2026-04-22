package server.network;

import server.controller.AuthServerController;
import server.controller.BidServerController;
import server.controller.AuctionServerController;
import shared.request.BaseRequest;
import shared.response.BaseResponse;

public class RequestRouter {

    public static BaseResponse route(BaseRequest request, ClientConnectionHandler handler) {
        String action = request.getAction();
        Object data = request.getData();

        System.out.println(">>> Đang điều phối hành động: " + action);

        switch (action)
        {
            case "LOGIN":
                // Sau này sẽ gọi AuthServerController.login(data)
                return AuthServerController.getInstance().login(request, handler);

            case "PLACE_BID":
                // Sau này sẽ gọi BidServerController.placeBid(data)
                return BidServerController.getInstance().placeBid(request, handler);

            case "SUBSCRIBE_AUCTION":
                int auctionId = Integer.parseInt(data.toString());
                RealtimePushServer.subscribeToAuction(auctionId, handler);
                return new BaseResponse(true, "Đã tham gia phòng đấu giá #" + auctionId, null);

            default:
                return new BaseResponse(false, "Hành động không xác định", null);
        }
    }
}