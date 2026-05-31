package server.model.item.factory;

import server.model.item.Item;
import shared.enums.ItemCategory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry gom tất cả concrete factory vào một chỗ.
 * Dùng khi cần tạo Item rỗng theo category (ví dụ: deserialize từ DB,
 * hoặc khởi tạo form nhập liệu).
 * Cách dùng:
 *   Item item = ItemFactoryRegistry.create(ItemCategory.ART);
 */
public class ItemFactoryRegistry {

    private static final Map<ItemCategory, ItemFactory> registry = new EnumMap<>(ItemCategory.class);

    static {
        registry.put(ItemCategory.ART,         new ArtFactory());
        registry.put(ItemCategory.ELECTRONICS, new ElectronicsFactory());
        registry.put(ItemCategory.VEHICLE,     new VehicleFactory());
    }

    /**
     * Tạo một Item rỗng (chỉ set category) theo loại truyền vào.
     *
     * @param category loại item cần tạo
     * @return Item con tương ứng (Art / Electronics / Vehicle)
     * @throws IllegalArgumentException nếu category chưa được đăng ký
     */
    public static Item create(ItemCategory category) {
        ItemFactory factory = registry.get(category);
        if (factory == null) {
            throw new IllegalArgumentException("Chưa có factory cho category: " + category);
        }
        return factory.createItem();
    }

    /** Kiểm tra category có được hỗ trợ không. */
    public static boolean isSupported(ItemCategory category) {
        return registry.containsKey(category);
    }
}