package shared.dto.request.item;

import shared.dto.request.BaseRequest;
import shared.enums.Action;
import shared.enums.ItemCategory;

import java.math.BigDecimal;

public class CreateItemRequest extends BaseRequest {
    private final String name;
    private final String description;
    private final ItemCategory category;
    private final BigDecimal priceStart;

    public CreateItemRequest(String name, String description,
                             ItemCategory category, BigDecimal priceStart) {
        super(Action.CREATE_ITEM);
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceStart = priceStart;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemCategory getCategory() { return category; }
    public BigDecimal getPriceStart() { return priceStart; }
}