
package server.network;

import server.controller.*;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.UserRole;

/**
 * RequestRouter - nơi nhận request từ client và điều phối tới các controller
 * có kiểm tra auth/role
 */
public class RequestRouter {

    //Custom Exception liên quan phần kiểm tra xác thực
    private static class UnauthorizedException extends RuntimeException {
        UnauthorizedException(String message) {
            super(message);
        }
    }
    private static void requireLogin(ClientConnectionHandler handler) {
        if (handler.getUser() == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để thực hiện hành động này!");
        }
    }
    private static void requireRole(ClientConnectionHandler handler, UserRole requiredRole) {
        requireLogin(handler);
        if (handler.getUser().getRole() != requiredRole) {
            throw new UnauthorizedException(String.format(
                    "Bạn không có quyền thực hiện hành động này! (Yêu cầu: %s)", requiredRole.name()));
        }
    }

    public static BaseResponse route(BaseRequest request, ClientConnectionHandler handler) {
        // 1. Kiểm tra request null để tránh Crash Server
        if (request == null || request.getAction() == null) {
            return new BaseResponse(false, "Yêu cầu không hợp lệ (Null Request/Action)", null);
        }

        Action action;

        try {
            action = request.getAction();
        } catch (IllegalArgumentException e) {
            return new BaseResponse(false, "Action không hợp lệ: " + request.getAction(), null);
        }

        System.out.println(">>> [Router] Đang điều phối hành động: " + action);

        try {
            switch (action) {
                /**
                 * Xác thực người dùng
                 */
                case Action.LOGIN: {
                    return AuthServerController.getInstance().login(request, handler);
                }

                case Action.REGISTER: {
                    return AuthServerController.getInstance().register(request);
                }

                case Action.LOGOUT: {
                    return AuthServerController.getInstance().logout(handler);
                }

                case Action.CHANGE_PASSWORD: {
                    requireLogin(handler);
                    return AuthServerController.getInstance().changePassword(request, handler);
                }

                /**
                 * Thao tác của ví
                 */

                case GET_WALLET: {
                    requireLogin(handler);
                    return WalletServerController.getInstance().getWallet(handler);
                }

                case DEPOSIT_WALLET: {
                    requireRole(handler, UserRole.BIDDER);
                    return WalletServerController.getInstance().deposit(request, handler);
                }

                case GET_SELLER_WALLET_SUMMARY: {
                    requireRole(handler, UserRole.SELLER);
                    return WalletServerController.getInstance().getSellerWalletSummary(handler);
                }

                case WITHDRAW_SELLER_WALLET: {
                    requireRole(handler, UserRole.SELLER);
                    return WalletServerController.getInstance().withdrawSellerWallet(request, handler);
                }

                /**
                 * Thao tác của Seller
                 */
                case Action.CREATE_ITEM: {
                    requireRole(handler, UserRole.SELLER);
                    return SellerServerController.getInstance().createItem(request, handler);
                }

                case Action.UPDATE_ITEM: {
                    requireRole(handler, UserRole.SELLER);
                    return SellerServerController.getInstance().updateItem(request);
                }

                case Action.DELETE_ITEM: {
                    requireRole(handler, UserRole.SELLER);
                    if (request.getData() == null) {
                        return new BaseResponse(false, "Thiếu dữ liệu itemId để xoá!", null);
                    }

                    try {
                        Long itemId = Long.parseLong(request.getData().toString());
                        return SellerServerController.getInstance().deleteItem(itemId);

                    } catch (NumberFormatException e) {
                        return new BaseResponse(false, "itemId phải là số nguyên hợp lệ!", null);
                    }
                }

                case Action.GET_SELLER_ITEMS: {
                    requireRole(handler, UserRole.SELLER);
                    return SellerServerController.getInstance().getItemsBySeller(handler);
                }
                case Action.GET_SELLER_APPROVED_AUCTIONS: {
                    requireRole(handler, UserRole.SELLER);
                    return AuctionServerController.getInstance().getApprovedAuctionsBySeller(handler);
                }
                case Action.SEND_CREATE_AUCTION_REQUEST: {
                    requireRole(handler, UserRole.SELLER);
                    return AuctionServerController.getInstance().createAuction(request, handler);

                }

                /**
                 * Thao tác của Bidder
                 */
                case Action.PLACE_BID: {
                    requireRole(handler, UserRole.BIDDER);
                    return BidServerController.getInstance().placeBid(request, handler);
                }
                case Action.GET_WON_AUCTIONS: {
                    requireRole(handler, UserRole.BIDDER);
                    return AuctionServerController.getInstance().getWonAuctions(handler);
                }
                case Action.FOLLOW_AUCTION: {
                    requireRole(handler, UserRole.BIDDER);
                    return AuctionServerController.getInstance().followAuction(request, handler);
                }
                case Action.JOIN_AUCTION: {
                    requireRole(handler, UserRole.BIDDER);
                    return AuctionServerController.getInstance().joinAuction(request, handler);
                }
                case Action.GET_INTERESTED_AUCTIONS: {
                    requireRole(handler, UserRole.BIDDER);
                    return AuctionServerController.getInstance().getInterestedAuctions(handler);
                }

                case Action.SUBSCRIBE_AUCTION: {
                    requireRole(handler, UserRole.BIDDER);
                    Object data = request.getData();
                    // 3. Ép kiểu an toàn để tránh NumberFormatException
                    if (data == null) {
                        return new BaseResponse(false, "Thiếu dữ liệu (ID phiên đấu giá)", null);
                    }

                    try {
                        Long auctionId = Long.parseLong(data.toString());
                        RealtimePushServer.subscribeToAuction(auctionId, handler);
                        return new BaseResponse(true,
                                String.format("Đã tham gia phòng đấu giá #%d", auctionId), null);

                    } catch (NumberFormatException e) {
                        return new BaseResponse(false,
                                "ID phiên đấu giá phải là số nguyên hợp lệ!", null);
                    }
                }
                case Action.GET_AUCTION_LIST:
                    return AuctionServerController.getInstance().getAuctions();
                case Action.GET_AUCTION_DETAILS:
                    return AuctionServerController.getInstance().getAuctionDetail(request);

                case Action.GET_BID_HISTORY:
                    return AuctionServerController.getInstance().getBidHistory(request);


                case Action.SUBSCRIBE_AUCTION_LIST:
                {
                    requireRole(handler, UserRole.BIDDER);
                    RealtimePushServer.subscribeToAuctionList(handler);
                    return new BaseResponse(true, "Đã subscribe danh sách phiên đấu giá ", null);
                }


                case Action.REGISTER_AUTO_BID_RULE: {
                    requireRole(handler, UserRole.BIDDER);
                    return AutoBidController.getInstance().registerRule(request, handler);
                }
                case Action.REMOVE_AUTO_BID_RULE: {
                    requireRole(handler, UserRole.BIDDER);
                    return AutoBidController.getInstance().removeRule(request, handler);
                }

                /**
                 * Thao tác của Admin
                 */

                case Action.GET_USERS_LIST: {
                    requireRole(handler, UserRole.ADMIN);
                    return AdminServerController.getInstance().getAllUsers();
                }

                case Action.GET_ALL_AUCTIONS: {
                    requireRole(handler, UserRole.ADMIN);
                    return AdminServerController.getInstance().getAllAuctions();
                }

                case Action.GET_ALL_ITEMS: {
                    requireRole(handler, UserRole.ADMIN);
                    return AdminServerController.getInstance().getAllItems();
                }

                case Action.ENABLE_USER: {
                    requireRole(handler, UserRole.ADMIN);
                    if (request.getData() == null) {
                        return new BaseResponse(false, "Thiếu dữ liệu userId để mở khóa!", null);
                    }
                    return AdminServerController.getInstance().enableUser(request);
                }

                case Action.DISABLE_USER: {
                    requireRole(handler, UserRole.ADMIN);
                    if (request.getData() == null) {
                        return new BaseResponse(false, "Thiếu dữ liệu userId để khóa!", null);
                    }
                    return AdminServerController.getInstance().disableUser(request);
                }

                case Action.GET_CREATE_AUCTION_REQUESTS: {
                    requireRole(handler, UserRole.ADMIN);
                    return AdminServerController.getInstance().getCreateAuctionRequests();
                }
                case Action.ACCEPT_CREATE_AUCTION_REQUEST: {
                    requireRole(handler, UserRole.ADMIN);
                    return AdminServerController.getInstance().acceptCreateAuctionRequest(request);
                }
                case Action.REJECT_CREATE_AUCTION_REQUEST: {
                    requireRole(handler, UserRole.ADMIN);
                    return AdminServerController.getInstance().rejectCreateAuctionRequest(request);
                }
                case Action.GET_ADMIN_DASHBOARD_STATS:
                    requireRole(handler, UserRole.ADMIN);
                    return AdminServerController.getInstance().getDashboardStats();
                default: {
                    return new BaseResponse(false,
                            "Hành động '" + action + "' không tồn tại trên hệ thống", null);
                }
            }
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi Server: " + e.getMessage(), null);
        }
    }
}
