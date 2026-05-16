package server.model.item;

import server.model.Entity;
import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Item extends Entity {

    private static final long serialVersionUID = 1L;

    protected String nameItem;
    protected String description;
    protected Long sellerId;
    protected BigDecimal priceStart;
    protected ItemCategory category;
    protected ItemStatus statusItem;
    protected LocalDateTime createdAtItem;
    protected String imageUrl;

    public Item() {}

    public Item(String nameItem, String description, Long sellerId,
                BigDecimal priceStart, ItemStatus statusItem) {
        super();
        this.nameItem = nameItem;
        this.description = description;
        this.sellerId = sellerId;
        if (priceStart.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price phải > 0");
        }
        else
            this.priceStart = priceStart;
        this.statusItem = statusItem != null ? statusItem : ItemStatus.PENDING;
    }

    //getter
    public String getNameItem() {
        return nameItem;
    }
    public String getDescription() {
        return description;
    }
    public Long getSellerId() {
        return sellerId;
    }
    public BigDecimal getPriceStart() {return priceStart;}
    public ItemCategory getCategory() {
        return category;
    }
    public ItemStatus getStatusItem() {
        return statusItem;
    }
    public LocalDateTime getCreatedAtItem() {
        return createdAtItem;
    }

    // setter
    public void setNameItem(String nameItem) {
        this.nameItem = nameItem;
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
    public void setCategory(ItemCategory category) {this.category = category;}
    public void setStatusItem(ItemStatus statusItem) { this.statusItem = statusItem; }
    public void setCreatedAtItem(LocalDateTime createdAtItem) {
        this.createdAtItem = createdAtItem;
    }

    @Override
    public String getInfo() {
        return String.format("[%s] %s – Starting price: %s",
                category,
                nameItem,
                priceStart.setScale(2, java.math.RoundingMode.HALF_UP)
        );
    }
}