package shared.dto.request.item;

import shared.dto.request.BaseRequest;
import shared.enums.Action;

public class GetSellerItemsRequest extends BaseRequest {
    public GetSellerItemsRequest() {
        super(Action.GET_SELLER_ITEMS);
    }
}