package server.service;

import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.ItemDAO;
import server.dao.UserDAO;
import server.model.core.Auction;
import server.model.core.Bid;
import server.model.item.Item;
import server.model.user.User;
import server.network.RealtimePushServer;
import shared.dto.common.AuctionDTO;
import shared.dto.common.BidDTO;
import shared.dto.request.BaseRequest;
import shared.dto.request.CreateAuctionRequest;
import shared.enums.AuctionStatus;
import shared.dto.response.BaseResponse;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AuctionService xử lí các nghiệp vụ liên quan tới phiên đấu giá
 * tức là đảm bảo 1 phiên đấu giá hoạt động đúng logic
 * Singleton đảm bảo tính nhất quán - dựa vào mức độ quan trọng trong hệ thống của AuctionService
 */

public class AuctionService {

    private static AuctionService instance = null;

    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BidDAO bidDAO = new BidDAO();

    private AuctionService() {}

    /**
     * sử dụng double-checked locking thay cho synchronized method
     * tức là: chỉ synchronized khi object chưa được tạo, còn tạo rồi thì không cần lock nữa
     * --> nhanh hơn
     */
    public static AuctionService getInstance() {
        //check lần 1, nếu tồn tại rồi thì return luôn
        if (instance == null) {

            synchronized (AuctionService.class) {
                //check lần 2, chưa tồn tại thì mới tạo
                if (instance == null) {
                    instance = new AuctionService();
                }
            }
        }
        return instance;
    }

    //----------------HELPER-----------------
    /**
     * chuyển dữ liệu Auction được nhận thành DTO gửi lại cho client
     */
    private AuctionDTO toDTO(Auction auction, Item item) {
        AuctionDTO dto = new AuctionDTO();

        dto.setId(auction.getId());

        dto.setItemId(item.getId());
        dto.setItemName(item.getNameItem());

        dto.setSellerId(auction.getSellerId());
        dto.setSellerName(userDAO.findById(item.getSellerId()).getFullName());

        dto.setStartTime(auction.getStartTime());
        dto.setCurrentPrice(auction.getMaxPrice() != null ? auction.getMaxPrice() : auction.getStartPrice());
        dto.setMinIncrement(auction.getMinIncrement());
        dto.setBuyNowPrice(auction.getBuyNowPrice());

        dto.setStatus(auction.getStatus());
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());

        return dto;
    }

    //----------------CREATE-----------------
    /**
     * tạo phiên đấu giá từ dữ liệu được gửi lên
     */
    public BaseResponse createAuction(Map<String, Object> data)
    {
        try {
            Long itemId = (Long) data.get("itemId");
            Long sellerId = (Long) data.get("sellerId");
            BigDecimal startPrice = (BigDecimal) data.get("startPrice");
            BigDecimal minIncrement = (BigDecimal) data.get("minIncrement");
            BigDecimal buyNowPrice = (BigDecimal) data.get("buyNowPrice");
            LocalDateTime startTime = (LocalDateTime) data.get("startTime");
            LocalDateTime endTime = (LocalDateTime) data.get("endTime");

            //kiểm tra xem item có tồn tại không?
            Item item = itemDAO.findById(itemId);
            if (item == null) {
                return new BaseResponse(false, "Sản phẩm không tồn tại!", null);
            }

            //kiểm tra rằng item chưa ở phiên đấu giá nào khác
            if (auctionDAO.existsOpenAuctionByItemId(itemId)) {
                return new BaseResponse(false, "Sản phẩm đang nằm ở phiên đấu giá khác!", null);
            }

            //kiểm tra logic thời gian start < end
            if (!endTime.isAfter(startTime)) {
                return new BaseResponse(false,
                                        "Thời gian kết thúc phải sau thời gian bắt đầu",
                                        null);
            }
            
            Auction auction = new Auction(itemId, sellerId, startPrice, null,
                                            minIncrement, buyNowPrice, startTime, endTime);

            boolean checkInsertAuction = auctionDAO.insertAuction(auction);
            if (!checkInsertAuction) {
                return new BaseResponse(false, "Lỗi lưu phiên đấu giá vào hệ thống", null);
            }

            //Chuển trạng thái của item trong Dao
            itemDAO.updateStatus(itemId, ItemStatus.ACTIVE);
            System.out.println(">>> [AuctionService] Tạo auction #" + auction.getId() + " cho item #" + itemId);

            return new BaseResponse(true, "Tạo phiên đấu giá thành công", toDTO(auction, item));
        }
        catch (Exception e) {
            //e.printStackTrace();
            return new BaseResponse(false, "Lỗi tạo phiên: " + e.getMessage(), null);
        }
    }

    //----------------CANCEL------------------
    public boolean cancelAuction(Long auctionId) {
        boolean ok = auctionDAO.cancelAuction(auctionId);
        if (ok) {
            Auction auction = auctionDAO.findById(auctionId);
            if (auction != null) {
                itemDAO.updateStatus(auction.getItemId(), ItemStatus.CANCELLED);
            }

            BaseResponse event = new BaseResponse(true,
                    String.format("Phiên đấu giá #%d đã bị huỷ!", auctionId),
                    null);

            RealtimePushServer.pushToAuctionSubscribers(auctionId, event);
            return new BaseResponse(true,
                    String.format("Đã hủy phiên #%d", auctionId),
                    null);
        }
        return new BaseResponse(false,
                String.format("Không thể hủy phiên #%d", auctionId),
                null);
    }

    //-------------------START-------------------
    /**
     * Mở phiên đấu giá (Chuyển status: preparing -> open)
     * synchronized đảm bảo các thread đều xem được trạng thái mới nhất của auction,
     * tránh logic lỗi mở auction 2 lần
     */
    public synchronized boolean startAuction(Long auctionId)
    {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            return false;
        }
        if (auction.getStatus() != AuctionStatus.PREPARING) {
            //không ở trạng thái preparing thì thôi return false
            return false;
        }

        boolean checkOpenAuction = auctionDAO.openAuction(auctionId);
        if (checkOpenAuction) {
            System.out.println(">>> [AuctionService] Đã mở auction #" + auctionId);

            //push thông báo cho tất cả client đang xem
            BaseResponse event = new BaseResponse(true, "AUCTION_STARTED",
                    "Phiên đấu giá #" + auctionId + " đã bắt đầu!");
            RealtimePushServer.pushToAuctionSubscribers(auctionId, event);
        }
        return checkOpenAuction;

    }

    //--------------FINISH------------------
    /**
     * kết thúc phiên đấu giá, chuyển trạng thái sang FINISHED
     * xác định người chiến thắng và trả thông tin
     */
    public synchronized BaseResponse finishAuction(Long auctionId)
    {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            return new BaseResponse(false, "Phiên đấu giá không tồn tại!", null);
        }

        //check trạng thái
        if (auction.getStatus() == AuctionStatus.FINISHED ||
            auction.getStatus() == AuctionStatus.CANCELLED) {
                return new BaseResponse(false, "Phiên đấu giá đã kết thúc trước đó!", null);
        }

        //cập nhật
        auctionDAO.updateStatus(auctionId, AuctionStatus.FINISHED);

        //Xác định winner từ highestBid, có rồi thì là item đã bán
        Bid highestBid = bidDAO.getHighestBidByAuctionId(auctionId);
        if (highestBid != null) {
            itemDAO.updateStatus(auction.getItemId(), ItemStatus.SOLD);
        }

        String winnerMsg;
        if (highestBid != null) {
            User winner = userDAO.findById(highestBid.getBidderId());

            //Nếu mà tìm thấy thì hiển thị tên, không thì hiển thị ID (trường hợp user đã bị xoá)
            // vì bid vẫn còn lưu bidderId
            String winnerName = (winner != null)
                    ? winner.getFullName()
                    : String.format("ID: %d", highestBid.getBidderId());

            winnerMsg = String.format("Phiên #%d kết thúc! Người thắng: %s với giá %s",
                    auctionId, winnerName, highestBid.getAmount());
        } else {
            winnerMsg = "Phiên #" + auctionId + " kết thúc. Không có ai đặt giá.";
        }

        System.out.println(">>> [AuctionService] " + winnerMsg);

        // Push cho tất cả client đang xem phiên này
        BaseResponse finishEvent = new BaseResponse(true, "AUCTION_FINISHED", winnerMsg);
        RealtimePushServer.pushToAuctionSubscribers(auctionId, finishEvent);

        return new BaseResponse(true, winnerMsg, highestBid);

    }

// ─── EXTEND (Anti-sniping) ────────────────────────────────────────────────

    /**
     * Gia hạn phiên khi có bid trong X giây cuối (Anti-sniping).
     * @param auctionId  ID phiên
     * @param extraSeconds Số giây gia hạn thêm (VD: 60)
     */
    public synchronized boolean extendAuction(Long auctionId, int extraSeconds) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return false;

        LocalDateTime newEnd = auction.getEndTime().plusSeconds(extraSeconds);
        auction.setEndTime(newEnd);

        boolean ok = auctionDAO.updateAuction(auction);
        if (ok) {
            String msg = String.format("Phiên #%d được gia hạn thêm %d giây! Kết thúc lúc %s",
                    auctionId, extraSeconds, newEnd);
            System.out.println(">>> [AuctionService] Anti-sniping: " + msg);

            BaseResponse event = new BaseResponse(true, "AUCTION_EXTENDED", msg);
            RealtimePushServer.pushToAuctionSubscribers(auctionId, event);
        }
        return ok;
    }

    //---------------GET--------------
    /** Lấy tất cả phiên đang OPEN */
    public BaseResponse getOpenAuctions() {
        try {
            List<Auction> auctions = auctionDAO.getAllAuctionsByStatus(AuctionStatus.OPEN);
            List<AuctionDTO> dtos = new ArrayList<>();
            for (Auction a : auctions) {
                Item item = itemDAO.findById(a.getItemId());
                dtos.add(toDTO(a, item));
            }
            return new BaseResponse(true,
                            "Lấy danh sách phiên đấu giá đang mở thành công",
                                    dtos);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                            String.format("Lỗi lấy danh sách: " + e.getMessage()),
                        null);
        }
    }

    /** Lấy tất cả phiên (dành cho Admin) */
    public BaseResponse getAllAuctions() {
        try {
            List<Auction> auctions = auctionDAO.getAllAuctions();
            List<AuctionDTO> dtos = new ArrayList<>();
            for (Auction a : auctions) {
                Item item = itemDAO.findById(a.getItemId());
                dtos.add(toDTO(a, item));
            }
            return new BaseResponse(true,
                            "Lấy danh sách tất cả phiên thành công!",
                                    dtos);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                            String.format("Lỗi lấy danh sách: %s", e.getMessage()),
                        null);
        }
    }

    /** Lấy chi tiết 1 phiên */
    public BaseResponse getAuctionDetail(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            return new BaseResponse(false,
                        String.format("Không tìm thấy phiên #%d", auctionId),
                    null);
        }

        Item item = itemDAO.findById(auction.getItemId());
        return new BaseResponse(true, "Lấy chi tiết phiên thành công!", toDTO(auction, item));
    }

    /** Lấy lịch sử bid của 1 phiên */
    public BaseResponse getBidHistory(Long auctionId) {
        try {
            List<Bid> bids = bidDAO.getAllBidsInAuctionId(auctionId);
            List<BidDTO> dtos = new ArrayList<>();
            for (Bid b : bids) {
                User bidder = userDAO.findById(b.getBidderId());
                String name = (bidder != null) ? bidder.getFullName() : "Ẩn danh";
                dtos.add(new BidDTO(b.getId(), b.getAuctionId(), b.getBidderId(),
                        name, b.getAmount(), b.getTimestamp()));
            }
            return new BaseResponse(true, "OK", dtos);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi lấy lịch sử bid: " + e.getMessage(), null);
        }
    }
}
