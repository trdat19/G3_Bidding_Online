package server.controller;

import server.service.AuctionService;
import server.network.ClientConnectionHandler;
import shared.dto.common.AuctionDTO;
import shared.dto.common.BidDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.util.List;
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
        List<AuctionDTO> auctions = auctionService.getOpenAuctions();

        if (auctions.isEmpty()) {
            return new BaseResponse(false, "Không có phiên đấu giá nào đang mở!", null);
        }

        return new BaseResponse(true, "Danh sách phiên đấu giá đang mở:", auctions);
    }

    /** getAllAuctions – Admin lấy tất cả phiên */
    public BaseResponse getAllAuctions() {
        List<AuctionDTO> auctions = auctionService.getAllAuctions();

        if (auctions.isEmpty()) {
            return new BaseResponse(false, "Không có phiên đấu giá nào!", null);
        }

        return new BaseResponse(true, "Danh sách tất cả phiên đấu giá:", auctions);
    }
    /** getWonAuctions - lấy phiên đã thắng */
    public BaseResponse getWonAuctions(ClientConnectionHandler handler) {
        List<AuctionDTO> auctions =
                auctionService.getWonAuctionsByBidderId(handler.getUser().getId());

        return new BaseResponse(
                true,
                auctions.isEmpty()
                        ? "Bạn chưa thắng phiên đấu giá nào."
                        : "Danh sách sản phẩm đã thắng:",
                auctions
        );
    }

    /** getAuctionDetail - lấy chi tiết thông tin của 1 phiên đấu giá */
    public BaseResponse followAuction(BaseRequest request, ClientConnectionHandler handler) {
        if (request.getData() == null) {
            return new BaseResponse(false, "Thiếu auctionId để theo dõi.", null);
        }
        try {
            Long auctionId = Long.parseLong(request.getData().toString());
            boolean saved = auctionService.followAuction(handler.getUser().getId(), auctionId);
            return new BaseResponse(saved,
                    saved ? "Đã thêm phiên đấu giá vào danh sách quan tâm."
                            : "Không thể theo dõi phiên đấu giá này.",
                    null);
        } catch (NumberFormatException e) {
            return new BaseResponse(false, "auctionId không hợp lệ.", null);
        }
    }

    public BaseResponse joinAuction(BaseRequest request, ClientConnectionHandler handler) {
        if (request.getData() == null) {
            return new BaseResponse(false, "Thiếu auctionId để tham gia.", null);
        }
        try {
            Long auctionId = Long.parseLong(request.getData().toString());
            boolean saved = auctionService.joinAuction(handler.getUser().getId(), auctionId);
            return new BaseResponse(saved,
                    saved ? "Đã ghi nhận phiên đấu giá bạn tham gia."
                            : "Không thể ghi nhận phiên đấu giá này.",
                    null);
        } catch (NumberFormatException e) {
            return new BaseResponse(false, "auctionId không hợp lệ.", null);
        }
    }

    public BaseResponse getInterestedAuctions(ClientConnectionHandler handler) {
        List<AuctionDTO> auctions =
                auctionService.getInterestedAuctionsByBidderId(handler.getUser().getId());
        return new BaseResponse(
                true,
                auctions.isEmpty() ? "Bạn chưa quan tâm phiên đấu giá nào."
                        : "Danh sách phiên đấu giá quan tâm:",
                auctions
        );
    }

    public BaseResponse getApprovedAuctionsBySeller(ClientConnectionHandler handler) {
        List<AuctionDTO> auctions =
                auctionService.getApprovedAuctionsBySellerId(handler.getUser().getId());
        return new BaseResponse(
                true,
                auctions.isEmpty() ? "Bạn chưa có phiên đấu giá nào được duyệt."
                        : "Danh sách phiên đấu giá đã được duyệt:",
                auctions
        );
    }

    public BaseResponse getAuctionDetail(BaseRequest request) {
        try {
            Long auctionId = Long.parseLong(request.getData().toString());
            AuctionDTO auction = auctionService.getAuctionDetail(auctionId);

            if (auction == null) {
                return new BaseResponse(false,
                        String.format("Không tìm thấy phiên đấu giá #%d", auctionId),
                        null);
            }

            return new BaseResponse(true, "Chi tiết phiên đấu giá:", auction);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi khi lấy chi tiết phiên đấu giá: %s",e.getMessage()),
                    null);
        }
    }

    /** createAuction – Seller tạo phiên mới */
    public BaseResponse createAuction(BaseRequest request, ClientConnectionHandler handler) {
        try {
            Map<String, Object> data = (Map<String, Object>) request.getData();
            if (data == null) {
                return new BaseResponse(false,
                        "Thieu thong tin can thiet de tao phien dau gia!", null);
            }
            data.put("sellerId", handler.getUser().getId());

            if (!(data.containsKey("startPrice")
                    && data.containsKey("minIncrement")
                    && data.containsKey("buyNowPrice")
                    && data.containsKey("startTime")
                    && data.containsKey("endTime")
                    && data.containsKey("itemId")
            ))
            {
                return new BaseResponse(false,
                        "Thiếu thông tin cần thiết để tạo phiên đấu giá!", null);
            }

            return new BaseResponse(true,
                    "Tạo phiên đấu giá thành công!",
                            auctionService.createAuction(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse(false, "Loi tao yeu cau dau gia: " + e.getMessage(), null);
        }
    }

    /** closeAuction – kết thúc phiên (Admin hoặc hết giờ) */
    public BaseResponse closeAuction(BaseRequest request) {
        try {
            if (request.getData() == null) {
                return new BaseResponse(false, "Thiếu auctionId để đóng phiên đấu giá!", null);
            }

            Long auctionId = Long.parseLong(request.getData().toString());
            Map<String, Object> data = auctionService.finishAuction(auctionId);

            return new BaseResponse(true,
                    String.format("Đã đóng phiên đấu giá #%d", auctionId),
                    data);

        } catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi đóng phiên: %s", e.getMessage()),
                    null);
        }
    }

    /** getBidHistory – lịch sử bid của 1 phiên */
    public BaseResponse getBidHistory(BaseRequest request) {
        try {
            if (request.getData() == null) {
                return new BaseResponse(false, "Thiếu auctionId để lấy lịch sử đấu giá!", null);
            }

            Long auctionId = Long.parseLong(request.getData().toString());
            List<BidDTO> bids = auctionService.getBidHistory(auctionId);

            return new BaseResponse(true,
                    String.format("Lịch sử đấu giá của phiên #%d", auctionId),
                    bids);

        } catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi lấy lịch sử: %s", e.getMessage()),
                    null);
        }
    }
}
