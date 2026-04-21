package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonth;
    private String model;
    private int year;

    public Electronics() {
        this.category = ItemCategory.ELECTRONICS;
    }

    public Electronics(String nameItem, String description, Long sellerId,
                       BigDecimal priceStart, ItemStatus statusItem) {
        super(nameItem, ItemCategory.ELECTRONICS, description, sellerId, priceStart, statusItem);
    }

    public Electronics(Long id, String nameItem, String description, Long sellerId,
                       BigDecimal priceStart, ItemStatus statusItem, Timestamp createdAtItem) {
        super(id, nameItem, ItemCategory.ELECTRONICS, description, sellerId, priceStart, statusItem, createdAtItem);
    }

    @Override
    public void printInfo() {
        System.out.println("Electronic: " + nameItem +
                "| Brand: " + brand +
                "| Model: " + model  +
                "| WarrantyMonth: " + warrantyMonth +
                "| Year: " + year);
    }
}