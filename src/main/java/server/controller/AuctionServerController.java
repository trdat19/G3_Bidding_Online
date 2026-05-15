package server.controller;

import server.service.AuctionService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.util.Map;

/**
 * AuctionServerController - các request liên quan đến Auction
 * Singleton
 */
public class AuctionServerController {
    private static AuctionServerController instance;
    private final AuctionService auctionService = AuctionService.getInstance();

    //double-checked locking để đảm bảo thread-safe khi khởi tạo instance
    public static AuctionServerController getInstance() {
        //check lần 1
        if (instance == null) {

            //check lần 2
            synchronized (AuctionServerController.class) {
                if (instance == null) {
                    instance = new AuctionServerController();
                }
            }
        }
        return instance;
    }

    /** getAuctions – lấy danh sách phiên đang OPEN */
    public BaseResponse getAuctions() {
        return auctionService.getOpenAuctions();
    }

    /** getAllAuctions – Admin lấy tất cả phiên */
    public BaseResponse getAllAuctions() {
        return auctionService.getAllAuctions();
    }

    /** getAuctionDetail - lấy chi tiết thông tin của 1 phiên đấu giá */
    public BaseResponse getAuctionDetail(BaseRequest request) {
        try {
            Long auctionId = Long.parseLong(request.getData().toString());
            return auctionService.getAuctionDetail(auctionId);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi khi lấy chi tiết phiên đấu giá: %s",e.getMessage()),
                    null);
        }
    }

    /** createAuction – Seller tạo phiên mới */
    public BaseResponse createAuction(BaseRequest request) {
        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            return auctionService.createAuction(data);
        } catch (ClassCastException e) {
            return new BaseResponse(false, "Dữ liệu phiên không hợp lệ!", null);
        }
    }

    /** closeAuction – kết thúc phiên (Admin hoặc hết giờ) */
    public BaseResponse closeAuction(BaseRequest request) {
        try {
            Long auctionId = Long.parseLong(request.getData().toString());
            return auctionService.finishAuction(auctionId);
        } catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi đóng phiên: %s", e.getMessage()),
                    null);
        }
    }

    /** getBidHistory – lịch sử bid của 1 phiên */
    public BaseResponse getBidHistory(BaseRequest request) {
        try {
            Long auctionId = Long.parseLong(request.getData().toString());
            return auctionService.getBidHistory(auctionId);
        } catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi lấy lịch sử: %s", e.getMessage()),
                    null);
        }
    }
}
