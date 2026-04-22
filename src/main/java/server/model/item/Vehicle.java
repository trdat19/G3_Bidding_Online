package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Vehicle extends Item {
    private String manufacturer;
    private int year;

    public Vehicle() {
        this.category = ItemCategory.VEHICLE;
    }

    public Vehicle(String nameItem, String description, Long sellerId,
                   BigDecimal priceStart, ItemStatus statusItem) {
        super(nameItem, ItemCategory.VEHICLE, description, sellerId, priceStart, statusItem);
    }

    public Vehicle(Long id, String nameItem, String description, Long sellerId,
                   BigDecimal priceStart, ItemStatus statusItem, Timestamp createdAtItem) {
        super(id, nameItem, ItemCategory.VEHICLE, description, sellerId, priceStart, statusItem, createdAtItem);
    }

    @Override
    public void printInfo() {
        System.out.println("Vehicle: " + nameItem +
                "| Manufacturer: " + manufacturer +
                "| Year: " + year);
    }
}
