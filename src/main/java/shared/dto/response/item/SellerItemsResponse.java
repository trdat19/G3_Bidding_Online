package shared.dto.response.item;

import shared.dto.ItemDTO;
import shared.dto.response.BaseResponse;

import java.util.List;

public class SellerItemsResponse extends BaseResponse {
    private final List<ItemDTO> items;

    public SellerItemsResponse(List<ItemDTO> items) {
        super(true, "Lấy danh sách sản phẩm thành công");
        this.items = items;
    }

    public List<ItemDTO> getItems() {
        return items;
    }
}