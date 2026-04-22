package shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

public class ItemDTO implements Serializable {
    private Long id;
    private String nameItem;
    private String description;
    private ItemCategory category;
    private ItemStatus statusItem;
    private Long sellerId;
    private BigDecimal priceStart;
    private Timestamp createdAtItem;

    public ItemDTO() {}

    public ItemDTO(Long id, String nameItem, ItemCategory category, String description,
                Long sellerId, BigDecimal priceStart, ItemStatus statusItem,
                Timestamp createdAtItem) {
        this.id = id;
        this.nameItem = nameItem;
        this.category = category;
        this.description = description;
        this.sellerId = sellerId;
        this.priceStart = priceStart;
        this.statusItem = statusItem;
        this.createdAtItem = createdAtItem;
    }

    //get
    public String getNameItem() {
        return nameItem;
    }
    public ItemCategory getCategory() {
        return category;
    }
    public String getDescription() {
        return description;
    }
    public Long getSellerId() {
        return sellerId;
    }
    public BigDecimal getPriceStart() {
        return priceStart;
    }
    public ItemStatus getStatusItem() {
        return statusItem;
    }
    public Timestamp getCreatedAtItem() {
        return createdAtItem;
    }

    // set
    public void setNameItem(String nameItem) {
        this.nameItem = nameItem;
    }
    public void setCategory(ItemCategory category) {
        this.category = category;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    public void setPriceStart(BigDecimal priceStart) {
        this.priceStart = priceStart;
    }
    public void setStatusItem(ItemStatus statusItem) {
        this.statusItem = statusItem;
    }
    public void setCreatedAtItem(Timestamp createdAtItem) {
        this.createdAtItem = createdAtItem;
    }
}
