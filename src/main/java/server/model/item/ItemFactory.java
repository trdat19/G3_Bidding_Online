package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ItemFactory {

    //tạo mới item
    public static Item createItem(ItemCategory category, String name, String description,
                                  Long sellerId, BigDecimal price, ItemStatus status) {

        return switch (category) {
            case ART -> new Art(name, description, sellerId, price, status);
            case ELECTRONICS -> new Electronics(name, description, sellerId, price, status);
            case VEHICLE -> new Vehicle(name, description, sellerId, price, status);
            default -> throw new IllegalArgumentException("Unknown item category: " + category);
        };
    }

    //tạo từ database
    public static Item createItemFromDb(Long id, ItemCategory category, String name, String description,
                                        Long sellerId, BigDecimal price, ItemStatus status,
                                        Timestamp createdAt) {
        return switch (category) {
            case ART -> new Art(id, name, description, sellerId, price, status, createdAt);
            case ELECTRONICS -> new Electronics(id, name, description, sellerId, price, status, createdAt);
            case VEHICLE -> new Vehicle(id, name, description, sellerId, price, status, createdAt);
            default -> throw new IllegalArgumentException("Unknown item category: " + category);
        };
    }


}
