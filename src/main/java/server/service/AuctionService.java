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
import shared.enums.AuctionStatus;
import shared.dto.response.BaseResponse;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final UserDAO userDAO =  new UserDAO();
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
        dto.setItemDescription(item.getDescription());
        dto.setItemCategory(item.getCategory().name());
        dto.setItemImageUrl(item.getImageUrl());
        dto.setStartPrice(auction.getStartPrice());

        Bid highestBid = bidDAO.getHighestBidByAuctionId(auction.getId());
        if (highestBid != null) {
            User leader = userDAO.findById(highestBid.getBidderId());
            dto.setLeaderId(highestBid.getBidderId());
            dto.setLeaderName(leader != null ? leader.getFullName() : "ID: " + highestBid.getBidderId());
        }
        dto.setBidCount(bidDAO.countBidByAuctionId(auction.getId()));
        dto.setSellerId(auction.getSellerId());
        dto.setSellerName(userDAO.findById(item.getSellerId()).getFullName());

        dto.setStartTime(auction.getStartTime());
        dto.setCurrentPrice(auction.getMaxPrice() != null
                            ? auction.getMaxPrice()
                            : auction.getStartPrice()
        );
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
    public AuctionDTO createAuction(Map<String, Object> data) {
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
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }
        //Kiểm tra xem sản phẩm còn được phép tạo phiên đấu giá không?
        if (item.getStatusItem() != ItemStatus.PENDING
                && item.getStatusItem() != ItemStatus.CANCELLED) {
            throw new RuntimeException("Chỉ có thể tạo đấu giá cho sản phẩm đang PENDING hoặc CANCELLED!");
        }

        //kiểm tra logic thời gian start < end
        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        if (minIncrement == null || minIncrement.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Bước nhảy giá phải lớn hơn 0");
        }

        clearReusableNoBidAuctions(itemId);

        Auction auction = new Auction(itemId, sellerId, startPrice, startPrice,
                                        minIncrement, buyNowPrice, startTime, endTime);

        boolean checkInsertAuction = auctionDAO.insertAuction(auction);
        if (!checkInsertAuction) {
            throw new RuntimeException("Lỗi lưu phiên đấu giá vào hệ thống");
        }

        //Chuển trạng thái của item trong Dao
        itemDAO.updateStatus(itemId, ItemStatus.WAITING_APPROVAL);
        System.out.println(">>> [AuctionService] Tạo auction #" + auction.getId() + " cho item #" + itemId);

        return toDTO(auction, item);
    }

    private void clearReusableNoBidAuctions(Long itemId) {
        List<Auction> oldAuctions = auctionDAO.getAllAuctionsByItemId(itemId);
        if (oldAuctions == null || oldAuctions.isEmpty()) {
            return;
        }

        for (Auction oldAuction : oldAuctions) {
            if (bidDAO.countBidByAuctionId(oldAuction.getId()) > 0) {
                throw new RuntimeException("Sản phẩm đã có lịch sử đấu giá, không thể tạo lại phiên mới!");
            }

            if (!isReusableNoBidAuction(oldAuction.getStatus())) {
                throw new RuntimeException("Sản phẩm này đã có yêu cầu/phiên đấu giá, không thể tạo thêm!");
            }
        }

        if (!auctionDAO.deleteAuctionsByItemId(itemId)) {
            throw new RuntimeException("Không thể dọn phiên đấu giá cũ của sản phẩm!");
        }
    }

    private boolean isReusableNoBidAuction(AuctionStatus status) {
        return status == AuctionStatus.FINISHED
                || status == AuctionStatus.CANCELLED
                || status == AuctionStatus.CLOSED;
    }

    //Thêm method admin lấy danh sách request
    public List<AuctionDTO> getCreateAuctionRequests() {
        List<Auction> auctions = auctionDAO.getAllAuctionsByStatus(AuctionStatus.PREPARING);
        List<AuctionDTO> dtos = new ArrayList<>();

        for (Auction auction : auctions) {
            Item item = itemDAO.findById(auction.getItemId());
            if (item != null) {
                dtos.add(toDTO(auction, item));
            }
        }
        return dtos;
    }
    //Thêm method admin duyệt request
    public AuctionDTO approveCreateAuctionRequest(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);

        if (auction == null) {
            throw new RuntimeException("Không tìm thấy yêu cầu đấu giá");
        }
        if (auction.getStatus() != AuctionStatus.PREPARING) {
            throw new RuntimeException("Chỉ duyệt được yêu cầu đang chờ duyệt");

        }
        LocalDateTime now = LocalDateTime.now();

        if (auction.getEndTime() != null && !now.isBefore(auction.getEndTime())) {
            throw new RuntimeException("Không thể duyệt phiên đã quá thời gian kết thúc!");
        }
        AuctionStatus nextStatus = AuctionStatus.OPEN;
        if (auction.getStartTime() != null && !now.isBefore(auction.getStartTime())) {
            nextStatus = AuctionStatus.RUNNING;
        }
        auctionDAO.updateStatus(auctionId, nextStatus);
        itemDAO.updateStatus(auction.getItemId(), ItemStatus.ACTIVE);
        auction.setStatus(nextStatus);

        Item item = itemDAO.findById(auction.getItemId());
        return toDTO(auction, item);
    }
    //Thêm method admin từ chối request
    public AuctionDTO rejectCreateAuctionRequest(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
           throw new RuntimeException("Không tìm thấy yêu cầu đấu giá");
        }
        if (auction.getStatus() != AuctionStatus.PREPARING)  {
           throw new RuntimeException("Chỉ duyệt được yêu cầu đang chờ duyệt");
        }
        auctionDAO.updateStatus(auctionId, AuctionStatus.CANCELLED);
        itemDAO.updateStatus(auction.getItemId(), ItemStatus.CANCELLED);

        auction.setStatus(AuctionStatus.CANCELLED);
        Item item = itemDAO.findById(auction.getItemId());
        return toDTO(auction,item);
    }


    //----------------CANCEL------------------
    public boolean cancelAuction(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);

        if (auction != null) {
            auctionDAO.updateStatus(auctionId, AuctionStatus.CANCELLED);
            itemDAO.updateStatus(auction.getItemId(), ItemStatus.CANCELLED);

            BaseResponse event = new BaseResponse(
                    true,
                    String.format("Phiên đấu giá #%d đã bị huỷ!", auctionId),
                    null
            );
            event.setAction("AUCTION_CANCELLED");

            RealtimePushServer.pushToAuctionSubscribers(auctionId, event);

            BaseResponse listEvent = new BaseResponse(
                    true,
                    "Danh sách phiên đấu giá đã thay đổi",
                    null
            );
            listEvent.setAction("AUCTION_LIST_CHANGED");

            RealtimePushServer.pushToAuctionListSubscribers(listEvent);

            return true;
        }

        return false;
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

        auction.setStatus(AuctionStatus.OPEN);

        boolean checkOpenAuction = auctionDAO.updateStatus(auctionId, AuctionStatus.OPEN);
        if (checkOpenAuction) {
            System.out.println(">>> [AuctionService] Đã mở auction #" + auctionId);

            //push thông báo cho tất cả client đang xem
            BaseResponse event = new BaseResponse(true, "Phiên đấu giá đã bắt đầu", null);
            event.setAction("AUCTION_STARTED");
            RealtimePushServer.pushToAuctionSubscribers(auctionId, event);

            // Thông báo danh sách phiên đấu giá thay đổi
            BaseResponse listEvent = new BaseResponse(
                    true,
                    "Danh sách phiên đấu giá đã thay đổi",
                    null
            );
            listEvent.setAction("AUCTION_LIST_CHANGED");

            RealtimePushServer.pushToAuctionListSubscribers(listEvent);
        }
        return checkOpenAuction;
    }

    //--------------FINISH------------------
    /**
     * kết thúc phiên đấu giá, chuyển trạng thái sang FINISHED
     * xác định người chiến thắng và trả thông tin
     */
    public synchronized Map<String, Object> finishAuction(Long auctionId) {
        Map<String, Object> data = new HashMap<>();

        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            return null;
        }

        //check trạng thái
        if (auction.getStatus() == AuctionStatus.FINISHED ||
            auction.getStatus() == AuctionStatus.CANCELLED) {
                return null;
        }

        //cập nhật
        auctionDAO.updateStatus(auctionId, AuctionStatus.FINISHED);

        //Xác định winner từ highestBid, có rồi thì là item đã bán
        Bid highestBid = bidDAO.getHighestBidByAuctionId(auctionId);
        if (highestBid != null) {
            WalletService.getInstance().payForWinningBid(
                    highestBid.getBidderId(),
                    highestBid.getAmount()
            );

            itemDAO.updateStatus(auction.getItemId(), ItemStatus.SOLD);
        }
        else
        {
            itemDAO.updateStatus(auction.getItemId(), ItemStatus.CANCELLED);
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
        BaseResponse finishEvent = new BaseResponse(
                true,
                winnerMsg,
                null
        );
        finishEvent.setAction("AUCTION_FINISHED");

        RealtimePushServer.pushToAuctionSubscribers(auctionId, finishEvent);


        BaseResponse listEvent = new BaseResponse(
                true,
                "Danh sách phiên đấu giá đã thay đổi",
                null
        );
        listEvent.setAction("AUCTION_LIST_CHANGED");

        RealtimePushServer.pushToAuctionListSubscribers(listEvent);

        data.put("message", winnerMsg);
        data.put("highestBid", highestBid);
        return data;
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

            BaseResponse event = new BaseResponse(
                    true,
                    msg,
                    newEnd
            );
            event.setAction("AUCTION_EXTENDED");

            RealtimePushServer.pushToAuctionSubscribers(auctionId, event);
        }
        return ok;
    }

    //---------------GET--------------
    /** Lấy tất cả phiên đang OPEN */
    public List<AuctionDTO> getOpenAuctions() {
        List<Auction> auctions = auctionDAO.getAllAuctionsByStatus(AuctionStatus.OPEN);
        auctions.addAll(auctionDAO.getAllAuctionsByStatus(AuctionStatus.RUNNING));
        List<AuctionDTO> dtos = new ArrayList<>();
        for (Auction a : auctions) {
            Item item = itemDAO.findById(a.getItemId());
            dtos.add(toDTO(a, item));
        }

        return dtos;
    }

    /** Lấy tất cả phiên (dành cho Admin) */
    public List<AuctionDTO> getAllAuctions() {
        List<Auction> auctions = auctionDAO.getAllAuctions();
        List<AuctionDTO> dtos = new ArrayList<>();
        for (Auction a : auctions) {
            Item item = itemDAO.findById(a.getItemId());
            dtos.add(toDTO(a, item));
        }

        return dtos;
    }
    public long countAllAuctions() {
        return auctionDAO.getAllAuctions().size();
    }

    /** Lấy chi tiết 1 phiên */
    public AuctionDTO getAuctionDetail(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            return null;
        }

        Item item = itemDAO.findById(auction.getItemId());
        return toDTO(auction, item);
    }

    /** Lấy lịch sử bid của 1 phiên */
    public List<BidDTO> getBidHistory(Long auctionId) {
        List<Bid> bids = bidDAO.getAllBidsInAuctionId(auctionId);
        List<BidDTO> dtos = new ArrayList<>();
        for (Bid b : bids) {
            User bidder = userDAO.findById(b.getBidderId());
            String name = (bidder != null) ? bidder.getFullName() : "Ẩn danh";
            dtos.add(new BidDTO(b.getId(), b.getAuctionId(), b.getBidderId(),
                    name, b.getAmount(), b.getTimestamp()));
        }

        return dtos;
    }

    public List<AuctionDTO> getAuctionApprovalRequests() {
        List<Auction> auctions = auctionDAO.getAllAuctionsByStatus(AuctionStatus.WAITING_APPROVAL);
        List<AuctionDTO> dtos = new ArrayList<>();

        for (Auction auction : auctions) {
            Item item = itemDAO.findById(auction.getItemId());
            dtos.add(toDTO(auction, item));
        }

        return dtos;
    }

    public boolean rejectAuctionRequest(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);

        if (auction == null || auction.getStatus() != AuctionStatus.WAITING_APPROVAL) {
            return false;
        }

        auctionDAO.updateStatus(auctionId, AuctionStatus.CANCELLED);
        itemDAO.updateStatus(auction.getItemId(), ItemStatus.CANCELLED);

        BaseResponse sellerEvent = new BaseResponse(
                true,
                "Trạng thái sản phẩm của seller đã thay đổi",
                auction.getItemId()
        );
        sellerEvent.setAction("SELLER_ITEMS_CHANGED");

        RealtimePushServer.pushToUser(auction.getSellerId(), sellerEvent);

        return true;
    }

    public boolean approveAuctionRequest(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null || auction.getStatus() != AuctionStatus.WAITING_APPROVAL) {
            return false;
        }
        boolean auctionUpdated = auctionDAO.updateStatus(auctionId, AuctionStatus.OPEN);
        boolean itemUpdated = itemDAO.updateStatus(auction.getItemId(), ItemStatus.ACTIVE);

        boolean ok = auctionUpdated && itemUpdated;

        if(ok)
        {
            BaseResponse event = new BaseResponse(true, "Danh sách phiên đấu giá đã có sự thay đổi", null);
            event.setAction("AUCTION_LIST_CHANGED");
            RealtimePushServer.pushToAuctionListSubscribers(event);

            BaseResponse sellerEvent = new BaseResponse(
                    true,
                    "Trạng thái sản phẩm của seller đã thay đổi",
                    auction.getItemId()
            );
            sellerEvent.setAction("SELLER_ITEMS_CHANGED");

            RealtimePushServer.pushToUser(auction.getSellerId(), sellerEvent);
        }

        return ok;
    }
}
