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
import shared.dto.request.CreateAuctionRequest;
import shared.dto.response.auction.CreateAuctionResponse;
import shared.enums.AuctionStatus;
import shared.dto.response.BaseResponse;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
     * sử dụng double-check locking thay cho synchronized method
     * tức là: chỉ synchronized khi object chưa được tạo, còn tạo rồi thì không cần lock nữa
     * --> nhanh hơn
     */
    public AuctionService getInstance() {
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
    public BaseResponse createAuction(CreateAuctionRequest data)
    {
        try {
            Long itemId = data.getItemId();
            Long sellerId = data.getSellerId();
            BigDecimal startPrice = data.getStartPrice();
            BigDecimal minIncrement = data.getMinIncrement();
            BigDecimal buyNowPrice = data.getBuyNowPrice();
            LocalDateTime startTime = data.getStartTime();
            LocalDateTime endTime = data.getEndTime();

            //kiểm tra xem item có tồn tại không?
            Item item = itemDAO.findById(itemId);
            if (item == null) {
                return new BaseResponse(false, "Sản phẩm không tồn tại!");
            }

            //kiểm tra rằng item chưa ở phiên đấu giá nào khác
            if (auctionDAO.existsOpenAuctionByItemId(itemId)) {
                return new BaseResponse(false, "Sản phẩm đang nằm ở phiên đấu giá khác!");
            }

            //kiểm tra logic thời gian start < end
            if (!endTime.isAfter(startTime)) {
                return new BaseResponse(false, "Thời gian kết thúc phải sau thời gian bắt đầu");
            }
            
            Auction auction = new Auction(itemId, sellerId, startPrice, null,
                                            minIncrement, buyNowPrice, startTime, endTime);

            boolean checkInsertAuction = auctionDAO.insertAuction(auction);
            if (!checkInsertAuction) {
                return new BaseResponse(false, "Lỗi lưu phiên đấu giá vào hệ thống");
            }

            //Chuển trạng thái của item trong Dao
            itemDAO.updateStatus(itemId, ItemStatus.ACTIVE);
            System.out.println(">>> [AuctionService] Tạo auction #" + auction.getId() + " cho item #" + itemId);

            return new CreateAuctionResponse( toDTO(auction, item));
        }
        catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse(false, "Lỗi tạo phiên: " + e.getMessage());
        }
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
//            BaseResponse event = new BaseResponse(true, "AUCTION_STARTED", "Phiên đấu giá #" + auctionId + " đã bắt đầu!");
            BaseResponse event = new BaseResponse(true,"Phiên đấu giá #" + auctionId + " đã bắt đầu!");
            RealtimePushServer.pushToAuctionSubscribers(auctionId.longValue(), event);
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
            return new BaseResponse(false, "Phiên đấu giá không tồn tại!");
        }

        //check trạng thái
        if (auction.getStatus() == AuctionStatus.FINISHED ||
            auction.getStatus() == AuctionStatus.CANCELLED) {
//                return new BaseResponse(false, "Phiên đấu giá đã kết thúc trước đó!", null);
                return new BaseResponse(false, "Phiên đấu giá đã kết thúc trước đó!");
        }

        //cập nhật
        auctionDAO.updateStatus(auctionId, AuctionStatus.FINISHED);

        //Xác định winner từ highestBid
        Bid highestBid = bidDAO.getHighestBidByAuctionId(auctionId);
        if (highestBid != null) {
            itemDAO.updateStatus(auction.getItemId(), ItemStatus.SOLD);
        }

        String winnerMsg;
        if (highestBid != null) {
            User winner = userDAO.findById(highestBid.getBidderId());
            String winnerName = (winner != null) ? winner.getFullName() : "ID:" + highestBid.getBidderId();
            winnerMsg = String.format("Phiên #%d kết thúc! Người thắng: %s với giá %s",
                    auctionId, winnerName, highestBid.getAmount());
        } else {
            winnerMsg = "Phiên #" + auctionId + " kết thúc. Không có ai đặt giá.";
        }

        System.out.println(">>> [AuctionService] " + winnerMsg);

        // Push cho tất cả client đang xem phiên này
//        BaseResponse finishEvent = new BaseResponse(true, "AUCTION_FINISHED", winnerMsg);
        BaseResponse finishEvent = new BaseResponse(true, winnerMsg);
        //RealtimePushServer.pushToAuctionSubscribers(auctionId.intValue(), finishEvent);

//        return new BaseResponse(true, winnerMsg, highestBid);
        return new BaseResponse(true, winnerMsg);

    }


    public void extendAuction()
    {

    }
    public void getOpenAuctions()
    {

    }
}
