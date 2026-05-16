package server.service;

import server.dao.ItemDAO;
import server.model.item.Item;
import server.model.item.ItemFactory;
import shared.dto.response.BaseResponse;
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

        //validate Item
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        if (category == null) {
            throw new IllegalArgumentException("Danh mục sản phẩm không được để trống!");
        }

        //static factory tạo item theo category
        Item item = ItemFactory.createItem(category, name, description, sellerId, ItemStatus.PENDING);

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

        // 3. Kiểm tra
        boolean ok = itemDAO.updateItem(item);

        return ok ? item : null;
    }

    public boolean deleteItem(Long itemId) {
        Item item = itemDAO.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại!");
        }

        // Chỉ cho phép xóa sản phẩm khi nó đang ở trạng thái PENDING
        if (item.getStatusItem() != ItemStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xóa sản phẩm khi đang PENDING!");
        }

        return itemDAO.deleteItem(itemId);
    }

    public List<Item> findBySeller(Long sellerId) {
        return itemDAO.findBySellerId(sellerId);
    }
}
