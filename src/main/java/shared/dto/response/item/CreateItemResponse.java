package shared.dto.response.item;

import shared.dto.ItemDTO;
import shared.dto.response.BaseResponse;

public class CreateItemResponse extends BaseResponse {
    private final ItemDTO item;

    public CreateItemResponse(ItemDTO item) {
        super(true, "Thêm sản phẩm thành công");
        this.item = item;
    }

    public ItemDTO getItem() {
        return item;
    }
}