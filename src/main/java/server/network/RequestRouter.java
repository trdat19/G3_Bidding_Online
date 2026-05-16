package server.network;

import server.controller.AuthServerController;
import server.controller.BidServerController;
import server.controller.AdminServerController;

import server.controller.SellerServerController;
import shared.dto.request.BaseRequest;
import shared.dto.request.auth.LoginRequest;
import shared.dto.request.auth.RegisterRequest;
import shared.dto.request.item.CreateItemRequest;
import shared.dto.request.item.DeleteItemRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.UserRole;

/**
 * Có nhiệm vụ điều phối Request từ client từng từng controller thích hợp
 */
public class RequestRouter {

    public static BaseResponse route(BaseRequest request, ClientConnectionHandler handler) {
        // 1. Kiểm tra request null để tránh Crash Server
        if (request == null || request.getAction() == null) {
            return new BaseResponse(false, "Yêu cầu không hợp lệ (Null Request)");
        }

        Action action = request.getAction();

        System.out.println(">>> [Router] Đang điều phối hành động: " + action);

        try {
            switch (action) {
                case Action.LOGIN:
                    return AuthServerController.getInstance().login((LoginRequest) request, handler);
                case Action.REGISTER:
                    return AuthServerController.getInstance().register((RegisterRequest) request);
                case CREATE_ITEM:
                    return new SellerServerController().createItem((CreateItemRequest) request, handler);
                case GET_SELLER_ITEMS:
                    return new SellerServerController().getSellerItems(handler);
                case DELETE_ITEM:
                    return new SellerServerController().deleteItem((DeleteItemRequest)request, handler);
                case Action.PLACE_BID:
                    // 2. Kiểm tra quyền: Chỉ người đã login mới được đặt Bid
                    if (handler.getUser() == null) {
                        return new BaseResponse(false, "Bạn cần đăng nhập để thực hiện đặt giá!");
                    }
                    return BidServerController.getInstance().placeBid(request, handler);
                case Action.SUBSCRIBE_AUCTION:
                    // 3. Ép kiểu an toàn để tránh NumberFormatException
//                    if (data == null) {
//                        return new BaseResponse(false, "Thiếu ID phiên đấu giá", null);
//                    }
//                    try {
//                        Long auctionId = Long.parseLong(data.toString());
//                        RealtimePushServer.subscribeToAuction(auctionId, handler);
//                        return new BaseResponse(true, "Đã tham gia phòng đấu giá #" + auctionId, null);
//                    } catch (NumberFormatException e) {
//                        return new BaseResponse(false, "ID phiên đấu giá phải là số nguyên", null);
//                    }
                    return new BaseResponse(false, "Chưa tạo SubscribeAuctionRequest");
//
                default:
                    return new BaseResponse(false, "Hành động '" + action + "' không tồn tại trên hệ thống");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse(false, "Lỗi Server: " + e.getMessage());
        }
    }
}