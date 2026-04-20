package server.model.item;

import server.model.Entity;
import shared.enums.ItemType;
import shared.enums.UserRole;

public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double basePrice;
    protected String sellerId;
    protected ItemType type;
    protected String idType;

    public Item(String name, String description, double basePrice, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.sellerId = sellerId;
    }

    public abstract String getType();
    public abstract void printInfo();

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getSellerId() {
        return sellerId;
    }
}

