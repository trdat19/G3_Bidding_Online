package server.service;

import server.dao.ItemDAO;
import server.model.item.Item;
import server.model.item.ItemFactory;
import server.model.user.User;
import shared.dto.ItemDTO;
import shared.dto.request.item.CreateItemRequest;
import shared.dto.response.BaseResponse;
import shared.dto.response.item.CreateItemResponse;
import shared.dto.response.item.SellerItemsResponse;
import shared.enums.ItemStatus;

import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO = new ItemDAO();

    public BaseResponse createItem(CreateItemRequest request, User seller) {
        try {
            Item item = ItemFactory.createItem(
                    request.getCategory(),
                    request.getName(),
                    request.getDescription(),
                    seller.getId(),
                    request.getPriceStart(),
                    ItemStatus.PENDING
            );

            boolean inserted = itemDAO.insertItem(item);
            if (!inserted) {
                return new BaseResponse(false, "Thêm sản phẩm thất bại");
            }
            ItemDTO dto = new ItemDTO(
                    item.getId(),
                    item.getNameItem(),
                    item.getDescription(),
                    item.getCategory(),
                    item.getStatusItem(),
                    item.getSellerId(),
                    seller.getFullName(),
                    item.getPriceStart(),
                    null,
                    item.getCreatedAtItem()
            );
            return new CreateItemResponse(dto);
        }
        catch (Exception e)
        {
            return new BaseResponse(false, "Đã xảy ra lỗi khi thêm sản phẩm");
        }
    }
    public void updateItem()
    {

    }
    public BaseResponse deleteItem(long itemId, User seller) {
        Item item = itemDAO.findById(itemId);

        if (item == null) {
            return new BaseResponse(false, "Không tìm thấy sản phẩm");
        }

        if (item.getSellerId() != seller.getId()) {
            return new BaseResponse(false, "Bạn không có quyền xóa sản phẩm này");
        }

        boolean deleted = itemDAO.deleteItem(itemId);

        if (deleted) {
            return new BaseResponse(true, "Xóa sản phẩm thành công");
        }

        return new BaseResponse(false, "Xóa sản phẩm thất bại");
    }
    public BaseResponse findBySeller(User seller)
    {
        try {
            List<Item> items = itemDAO.findBySellerId(seller.getId());

            List<ItemDTO> itemDTOs = items.stream()
                    .map(item -> new ItemDTO(
                            item.getId(),
                            item.getNameItem(),
                            item.getDescription(),
                            item.getCategory(),
                            item.getStatusItem(),
                            item.getSellerId(),
                            seller.getFullName(),
                            item.getPriceStart(),
                            null,
                            item.getCreatedAtItem()
                    ))
                    .toList();

            return new SellerItemsResponse(itemDTOs);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }
}
