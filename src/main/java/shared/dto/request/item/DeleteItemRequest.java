package shared.dto.request.item;

import shared.dto.request.BaseRequest;
import shared.enums.Action;

public class DeleteItemRequest extends BaseRequest {
    private final long itemId;

    public DeleteItemRequest(long itemId) {
        super(Action.DELETE_ITEM);
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
    }
}