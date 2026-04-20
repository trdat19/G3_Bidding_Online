package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Art extends Item {
    public Art() {
        this.category = ItemCategory.ART;
    }
    public Art(String nameItem, String description, Long sellerId,
               BigDecimal priceStart, ItemStatus statusItem) {
        super(nameItem, ItemCategory.ART, description, sellerId, priceStart, statusItem);
    }
    public Art(Long id, String nameItem, String description, Long sellerId,
               BigDecimal priceStart, ItemStatus statusItem, Timestamp createdAtItem) {
        super(id, nameItem, ItemCategory.ART, description, sellerId, priceStart, statusItem, createdAtItem);
    }
}