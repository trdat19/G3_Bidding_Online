package server.controller;

import server.model.item.Item;
import server.network.ClientConnectionHandler;
import server.service.ItemService;

import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.util.List;
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

    private final ItemService itemService = ItemService.getInstance();

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
            Long sellerId = handler.getUser().getId(); // Id lấy từ session
            // request.getData() là Map<String, Object> chứa {name, category, description}

            Item item = itemService.createItem(sellerId, (Map<String, Object>) request.getData());

            if (item != null) {
                System.out.println(">>> [SellerController] Thêm item #" + item.getId() + ": " + item.getNameItem());
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
            Map<String, Object> updateData = (Map<String, Object>) request.getData();

            Item updatedItem = itemService.updateItem(updateData);
            if (updatedItem != null) {
                System.out.println(">>> [SellerController] Cập nhật item #"
                        + updatedItem.getId() + ": " + updatedItem.getNameItem());
                return new BaseResponse(true, "Cập nhật sản phẩm thành công!", updatedItem);
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
            boolean ok = itemService.deleteItem(itemId);

            if (ok) {
                System.out.println(">>> [SellerController] Xóa item #" + itemId);
                return new BaseResponse(true, "Xóa sản phẩm thành công!", null);
            }

            return new BaseResponse(false, "Lỗi xóa sản phẩm khỏi database!", null);

        } catch (IllegalArgumentException e) {
            return new BaseResponse(false, e.getMessage(), null);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi xóa sản phẩm: " + e.getMessage(), null);
        }
    }

    // ---------------------- GET ALL BY SELLER ----------------------

    public BaseResponse getItemsBySeller(ClientConnectionHandler handler) {
        try {
            Long sellerId = handler.getUser().getId();

            List<Item> items = itemService.findBySeller(sellerId);
            return new BaseResponse(true, "Lấy danh sách sản phẩm thành công!", items);

        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi lấy danh sách sản phẩm: " + e.getMessage(), null);
        }
    }
}
