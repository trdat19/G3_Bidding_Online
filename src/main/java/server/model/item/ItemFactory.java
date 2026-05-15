package server.model.item;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Static factory — dùng khi cần tạo Item đầy đủ thông tin ngay lập tức
 * (ví dụ: nhận data từ client, tạo từ ResultSet DB).
 *
 * Khác với factory pattern (ItemFactoryRegistry) chuyên tạo object rỗng,
 * class này nhận đủ tham số và trả về Item đã được populate.
 */
public class ItemFactory {

    private ItemFactory() {} //không cho khởi tạo

    // -------------------------------------------------------------------------
    // Tạo Item mới (chưa có id, chưa có createdAt)
    // -------------------------------------------------------------------------

    /**
     * Tạo Item với các thuộc tính cơ bản chung cho mọi loại.
     *
     * @param category  loại item (ART / ELECTRONICS / VEHICLE)
     * @param name      tên item
     * @param description mô tả
     * @param sellerId  id seller
     * @param price     giá khởi điểm (phải > 0)
     * @param status    trạng thái item
     * @return Item con phù hợp với category
     */
    public static Item createItem(ItemCategory category,
                                  String name,
                                  String description,
                                  Long sellerId,
                                  BigDecimal price,
                                  ItemStatus status) {
        return switch (category) {
            case ART         -> new Art(name, description, sellerId, price, status);
            case ELECTRONICS -> new Electronics(name, description, sellerId, price, status);
            case VEHICLE     -> new Vehicle(name, description, sellerId, price, status);
        };
    }

    // -------------------------------------------------------------------------
    // Tạo Item từ DB (có id và createdAt)
    // -------------------------------------------------------------------------

    /**
     * Dùng khi map từ ResultSet.
     * Gọi {@link #createItem} rồi gán thêm id và createdAt.
     */
    public static Item createItemFromDb(Long id,
                                        ItemCategory category,
                                        String name,
                                        String description,
                                        Long sellerId,
                                        BigDecimal price,
                                        ItemStatus status,
                                        Timestamp createdAt) {
        Item item = createItem(category, name, description, sellerId, price, status);
        item.setId(id);
        if (createdAt != null) {
            item.setCreatedAtItem(createdAt.toLocalDateTime());
        }
        return item;
    }

    // -------------------------------------------------------------------------
    // Overloads tiện lợi cho từng loại cụ thể
    // -------------------------------------------------------------------------

    /** Tạo Art item và set ngay các thuộc tính riêng. */
    public static Art createArt(String name, String description, Long sellerId,
                                BigDecimal price, ItemStatus status,
                                String artist, int year, String material) {
        Art art = new Art(name, description, sellerId, price, status);
        art.setArtist(artist);
        art.setYear(year);
        art.setMaterial(material);
        return art;
    }

    /** Tạo Electronics item và set ngay các thuộc tính riêng. */
    public static Electronics createElectronics(String name, String description, Long sellerId,
                                                BigDecimal price, ItemStatus status,
                                                String brand, String model,
                                                int year, int warrantyMonth) {
        Electronics e = new Electronics(name, description, sellerId, price, status);
        e.setBrand(brand);
        e.setModel(model);
        e.setYear(year);
        e.setWarrantyMonth(warrantyMonth);
        return e;
    }

    /** Tạo Vehicle item và set ngay các thuộc tính riêng. */
    public static Vehicle createVehicle(String name, String description, Long sellerId,
                                        BigDecimal price, ItemStatus status,
                                        String manufacturer, int year) {
        Vehicle v = new Vehicle(name, description, sellerId, price, status);
        v.setManufacturer(manufacturer);
        v.setYear(year);
        return v;
    }
}