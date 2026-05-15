package shared.dto.common;

import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ItemDTO – dữ liệu sản phẩm truyền giữa client và server.
 *
 * Dùng khi:
 *   - Seller tạo / sửa sản phẩm (client → server)
 *   - Bidder xem danh sách sản phẩm (server → client)
 *   - AuctionDTO nhúng thông tin sản phẩm
 *
 * Không chứa các trường đặc thù của từng loại (artist, brand…)
 * vì client chỉ cần thông tin chung để hiển thị.
 */
public class ItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private ItemCategory category;
    private ItemStatus status;
    private Long sellerId;
    private String sellerName;      // Tên seller để hiển thị, không cần query thêm
    private BigDecimal priceStart;
    private String imageUrl;
    private LocalDateTime createdAt;

    // ─── CONSTRUCTORS ─────────────────────────────────────────────────────────

    public ItemDTO() {}

    /** Constructor đầy đủ */
    public ItemDTO(Long id, String name, String description,
                   ItemCategory category, ItemStatus status,
                   Long sellerId, String sellerName,
                   BigDecimal priceStart, String imageUrl,
                   LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.priceStart = priceStart;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    /** Constructor rút gọn – dùng khi Seller tạo item mới */
    public ItemDTO(String name, String description, ItemCategory category,
                   BigDecimal priceStart) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceStart = priceStart;
        this.status = ItemStatus.PENDING;
    }

    // ─── GETTERS ──────────────────────────────────────────────────────────────

    public Long getId()               { return id; }
    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public ItemCategory getCategory() { return category; }
    public ItemStatus getStatus()     { return status; }
    public Long getSellerId()         { return sellerId; }
    public String getSellerName()     { return sellerName; }
    public BigDecimal getPriceStart() { return priceStart; }
    public String getImageUrl()       { return imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ─── SETTERS ──────────────────────────────────────────────────────────────

    public void setId(Long id)                     { this.id = id; }
    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(ItemCategory category) { this.category = category; }
    public void setStatus(ItemStatus status)       { this.status = status; }
    public void setSellerId(Long sellerId)         { this.sellerId = sellerId; }
    public void setSellerName(String sellerName)   { this.sellerName = sellerName; }
    public void setPriceStart(BigDecimal price)    { this.priceStart = price; }
    public void setImageUrl(String imageUrl)       { this.imageUrl = imageUrl; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }

    @Override
    public String toString() {
        return String.format("ItemDTO[id=%d, name=%s, category=%s, price=%s, status=%s]",
                id, name, category, priceStart, status);
    }
}