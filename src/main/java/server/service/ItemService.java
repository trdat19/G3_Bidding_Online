package server.service;

import server.dao.ItemDAO;
import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.model.core.Auction;
import server.model.item.Item;
import server.model.item.ItemFactory;
import shared.dto.common.AuctionDTO;
import shared.dto.common.ItemDTO;
import shared.dto.response.BaseResponse;
import shared.enums.AuctionStatus;
import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Xử lí logic các thao tác liên quan tới sản phẩm
 *
 * Singleton
 */
public class ItemService {
    private static ItemService instance;

    private final ItemDAO itemDAO = new ItemDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidDAO bidDAO = new BidDAO();

    private ItemService() {}

    public static ItemService getInstance() {
        if (instance == null) {
            synchronized (ItemService.class) {
                if (instance == null) {
                    instance = new ItemService();
                }
            }
        }
        return instance;
    }

    //-----------CREATE------------
    public Item createItem(Long sellerId, Map<String, Object> data) {

        // kiểm tra đầy đủ trường dữ liệu
        if (!data.containsKey("name")
                || !data.containsKey("description")
                || !data.containsKey("category"))
            {
            throw new IllegalArgumentException("Thiếu thông tin cần thiết để tạo sản phẩm!");
        }

        String name          = data.get("name").toString();
        String description   = data.get("description").toString();
        ItemCategory category = ItemCategory.valueOf(data.get("category").toString());
        String imageUrl = data.containsKey("imageUrl") ? data.get("imageUrl").toString() : null;
        byte[] imageBytes = data.containsKey("imageBytes") ? (byte[]) data.get("imageBytes") : null;
        String imageContentType = data.containsKey("imageContentType")
                ? data.get("imageContentType").toString()
                : null;

        //validate Item
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        if (category == null) {
            throw new IllegalArgumentException("Danh mục sản phẩm không được để trống!");
        }

        //static factory tạo item theo category
        Item item = ItemFactory.createItem(category, name, description, sellerId, ItemStatus.PENDING);
        item.setImageUrl(imageUrl);
        item.setImageBytes(imageBytes);
        item.setImageContentType(imageContentType);

        boolean ok = itemDAO.insertItem(item);
        return ok ? item : null;
    }


    public Item updateItem(Map<String, Object> data) {
        // 1. Kiểm tra xem Id sản phẩm có tồn tại không?
        if (!data.containsKey("id")) {
            throw new IllegalArgumentException("Thiếu id sản phẩm cần cập nhật!");
        }

        Long itemId = (Long) data.get("id");
        Item item = itemDAO.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại!");
        }

        // 2. Xem Map<String, Object> data có những trường nào.

        String name        = data.containsKey("name") ? data.get("name").toString() : null;
        String description = data.containsKey("description") ? data.get("description").toString() : null;
        ItemCategory category   = data.containsKey("category")
                ? ItemCategory.valueOf(data.get("category").toString())
                : null;
        String imageUrl = data.containsKey("imageUrl") ? data.get("imageUrl").toString() : null;

        if (name != null) { item.setNameItem(name); }
        if (description != null) { item.setDescription(description); }
        if (category != null) { item.setCategory(category); }
        if (imageUrl != null) { item.setImageUrl(imageUrl); }
        if (data.containsKey("imageBytes")) {
            item.setImageBytes((byte[]) data.get("imageBytes"));
        }
        if (data.containsKey("imageContentType")) {
            item.setImageContentType(data.get("imageContentType").toString());
        }

        // 3. Kiểm tra
        boolean ok = itemDAO.updateItem(item);

        return ok ? item : null;
    }

    public boolean deleteItem(Long itemId) {
        Item item = itemDAO.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại!");
        }

        if (item.getStatusItem() != ItemStatus.PENDING
                && item.getStatusItem() != ItemStatus.CANCELLED) {
            throw new IllegalStateException("Chỉ có thể xóa sản phẩm khi đang PENDING hoặc CANCELLED!");
        }

        deleteNoBidAuctionsForItem(itemId);
        return itemDAO.deleteItem(itemId);
    }

    public List<Item> findBySeller(Long sellerId) {
        return itemDAO.findBySellerId(sellerId);
    }

    public List<Item> findAllItems() {
        return itemDAO.getAllItems();
    }

    public List<ItemDTO> findAllItemsForAdmin() {
        return itemDAO.getAdminItemSummaries();
    }
    public long countAllItems() {
        return itemDAO.countAllItems();
    }


    private void deleteNoBidAuctionsForItem(Long itemId) {
        List<Auction> auctions = auctionDAO.getAllAuctionsByItemId(itemId);
        if (auctions == null || auctions.isEmpty()) {
            return;
        }

        for (Auction auction : auctions) {
            if (bidDAO.countBidByAuctionId(auction.getId()) > 0) {
                throw new IllegalStateException("Không thể xóa sản phẩm đã có lượt đấu giá!");
            }

            if (!isInactiveAuctionStatus(auction.getStatus())) {
                throw new IllegalStateException("Không thể xóa sản phẩm khi còn yêu cầu/phiên đấu giá đang hoạt động!");
            }
        }

        if (!auctionDAO.deleteAuctionsByItemId(itemId)) {
            throw new IllegalStateException("Không thể xóa các phiên đấu giá cũ của sản phẩm!");
        }
    }

    private boolean isInactiveAuctionStatus(AuctionStatus status) {
        return status == AuctionStatus.FINISHED
                || status == AuctionStatus.CANCELLED
                || status == AuctionStatus.CLOSED;
    }
    //Method lấy auction mới nhất
    public AuctionDTO findLatestAuctionSummaryByItemId(Long itemId) {
        List<Auction> auctions = auctionDAO.getAllAuctionsByItemId(itemId);

        if (auctions == null || auctions.isEmpty()) {
            return null;
        }

        Auction latestAuction = auctions.get(0);

        AuctionDTO dto = new AuctionDTO();
        dto.setId(latestAuction.getId());
        dto.setItemId(latestAuction.getItemId());
        dto.setStartPrice(latestAuction.getStartPrice());
        dto.setCurrentPrice(
                latestAuction.getMaxPrice() != null
                        ? latestAuction.getMaxPrice()
                        : latestAuction.getStartPrice()
        );

        return dto;
    }
}
