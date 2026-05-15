package server.controller;

import server.dao.ItemDAO;
import server.model.item.Item;
import server.model.item.ItemFactory;
import server.network.ClientConnectionHandler;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Xử lý các thao tác của Seller
 *  - Tạo item mới
 *  - Cập nhật item đã tạo
 *  - Xóa item đã tạo
 *  - Lấy tất cả các items
 *
 *  Singleton
 */

public class SellerServerController {

    private static SellerServerController instance;
    private ItemDAO itemDAO = new ItemDAO();

    private SellerServerController() {}

    //double-checked locking
    public static SellerServerController getInstance() {
        if (instance == null) {

            synchronized (SellerServerController.class) {
                if (instance == null) {
                    instance = new SellerServerController();
                }
            }
        }
        return instance;
    }

    //------------------CREATE-----------------
    public BaseResponse createItem(BaseRequest request, ClientConnectionHandler handler){
        try {
            // request.getData() là Map<String, Object> chứa {name, category, description, startingPrice}
            // 1. Bóc tách dữ liệu
            Map<String, Object> data = (Map<String, Object>) request.getData();

            String name        = data.get("name").toString();
            String description = data.get("description").toString();
            Long sellerId      = handler.getUser().getId(); // lấy từ session
            BigDecimal priceStart   = new BigDecimal(data.get("priceStart").toString());
            ItemCategory category   = ItemCategory.valueOf(data.get("category").toString());

            // 2. Tạo item mới và lưu vào DB, trả kết quả về client
            //static factory
            Item item = ItemFactory.createItem(category, name, description, sellerId, priceStart, ItemStatus.PENDING);
            boolean ok = itemDAO.insertItem(item);

            if (ok) {
                System.out.println(">>> [SellerController] Thêm item #" + item.getId() + ": " + name);
                return new BaseResponse(true, "Thêm sản phẩm thành công!", item);
            }

            return new BaseResponse(false, "Lỗi lưu sản phẩm vào database!", null);

        } catch (Exception e) {
            return new BaseResponse(false,
                        String.format("Lỗi tạo sản phẩm: %s", e.getMessage()),
                    null);
        }
    }

    //--------------UPDATE - Sửa thông tin-------------------

    public BaseResponse updateItem(BaseRequest request) {
        //Không cần truyền Handler sản phẩm được sửa đã tồn tại, đã biết sellerId ở trong
        try {
            // request.getData() là Map<String, Object> chứa {itemId, name?, category?, description?, priceStart?}
            // 1. Lấy id kiểm tra trước xem có tồn tại sp không?
            Map<String, Object> data = (Map<String, Object>) request.getData();
            Long itemId = Long.parseLong(data.get("itemId").toString());

            Item item = itemDAO.findById(itemId);
            if (item == null) {
                return new BaseResponse(false, "Sản phẩm không tồn tại!", null);
            }

            // 2. Bóc tách dữ liệu (có thể có trường nào cần sửa, trường nào không)

            String name        = data.containsKey("name") ? data.get("name").toString() : null;
            String description = data.containsKey("description") ? data.get("description").toString() : null;
            BigDecimal priceStart   = data.containsKey("priceStart")
                                        ? new BigDecimal(data.get("priceStart").toString())
                                        : null;
            ItemCategory category   = data.containsKey("category")
                                        ? ItemCategory.valueOf(data.get("category").toString())
                                        : null;
            String imageUrl = data.containsKey("imageUrl") ? data.get("imageUrl").toString() : null;

            if (name != null) { item.setNameItem(name); }
            if (description != null) { item.setDescription(description); }
            if (priceStart != null) { item.setPriceStart(priceStart); }
            if (category != null) { item.setCategory(category); }
            if (imageUrl != null) { item.setImageUrl(imageUrl); }

            // 3. Kiểm tra
            boolean ok = itemDAO.updateItem(item);
            if (ok) {
                System.out.println(">>> [SellerController] Cập nhật item #" + item.getId() + ": " + name);
                return new BaseResponse(true, "Cập nhật sản phẩm thành công!", item);
            }

            return new BaseResponse(false, "Lỗi cập nhật sản phẩm vào database!", null);

        } catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi cập nhật sản phẩm: %s", e.getMessage()),
                    null);
        }
    }

    //--------------------DELETE------------------------
    //khi sp chưa vào phiên thì mới có thể xoá
    public BaseResponse deleteItem(Long itemId) {
        try {
            Item item = itemDAO.findById(itemId);
            if (item == null) {
                return new BaseResponse(false, "Sản phẩm không tồn tại!", null);
            }

            boolean ok = itemDAO.deleteItem(itemId);
            if (ok) {
                System.out.println(">>> [SellerController] Xóa item #" + itemId);
                return new BaseResponse(true, "Xóa sản phẩm thành công!", null);
            }

            return new BaseResponse(false, "Lỗi xóa sản phẩm khỏi database!", null);

        } catch (Exception e) {
            return new BaseResponse(false,
                    String.format("Lỗi xóa sản phẩm: %s", e.getMessage()),
                    null);
        }
    }
}
