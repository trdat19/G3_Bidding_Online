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
    protected ItemCategory category;
    protected ItemStatus statusItem;
    protected LocalDateTime createdAtItem;
    protected String imageUrl;

    public Item() {}

    public Item(String nameItem, String description, Long sellerId,
                ItemCategory category, ItemStatus statusItem) {
        super();
        this.nameItem = nameItem;
        this.description = description;
        this.category = category;
        this.sellerId = sellerId;
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
    public ItemCategory getCategory() {
        return category;
    }
    public ItemStatus getStatusItem() {
        return statusItem;
    }
    public LocalDateTime getCreatedAtItem() {
        return createdAtItem;
    }
    public String getImageUrl() { return imageUrl; }

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
    public void setCategory(ItemCategory category) {this.category = category;}
    public void setStatusItem(ItemStatus statusItem) { this.statusItem = statusItem; }
    public void setCreatedAtItem(LocalDateTime createdAtItem) {
        this.createdAtItem = createdAtItem;
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String getInfo() {
        return String.format("[%s] %s – %s",
                category,
                nameItem,
                description
        );
    }
}