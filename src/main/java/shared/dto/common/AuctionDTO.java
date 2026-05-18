package shared.dto.common;

import shared.enums.AuctionStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AuctionDTO – dữ liệu phiên đấu giá truyền giữa client và server.
 *
 * Dùng khi:
 *   - Hiển thị danh sách phiên (BidderDashboard, AdminDashboard)
 *   - Hiển thị chi tiết phiên (AuctionDetailController)
 *   - Cập nhật realtime khi có bid mới hoặc phiên thay đổi trạng thái
 *
 * Nhúng luôn thông tin item và người dẫn đầu
 * để client không phải gửi thêm request phụ.
 */
public class AuctionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // Thông tin sản phẩm (nhúng sẵn)
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private String itemImageUrl;

    // Thông tin seller
    private Long sellerId;
    private String sellerName;

    // Giá cả
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal minIncrement;
    private BigDecimal buyNowPrice;

    // Trạng thái & thời gian
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Người dẫn đầu
    private Long leaderId;
    private String leaderName;

    // Thống kê
    private int bidCount;

    // ─── CONSTRUCTORS ─────────────────────────────────────────────────────────

    public AuctionDTO() {}

    /** Constructor đầy đủ */
    public AuctionDTO(Long id, Long itemId, String itemName,
                      Long sellerId, String sellerName,
                      BigDecimal startPrice, BigDecimal currentPrice,
                      BigDecimal minIncrement, BigDecimal buyNowPrice,
                      AuctionStatus status,
                      LocalDateTime startTime, LocalDateTime endTime,
                      Long leaderId, String leaderName,
                      int bidCount) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.minIncrement = minIncrement;
        this.buyNowPrice = buyNowPrice;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.bidCount = bidCount;
    }

    // ─── GETTERS ──────────────────────────────────────────────────────────────

    public Long getId()                  { return id; }
    public Long getItemId()              { return itemId; }
    public String getItemName()          { return itemName; }
    public String getItemDescription()   { return itemDescription; }
    public String getItemCategory()      { return itemCategory; }
    public String getItemImageUrl()      { return itemImageUrl; }
    public Long getSellerId()            { return sellerId; }
    public String getSellerName()        { return sellerName; }
    public BigDecimal getStartPrice()    { return startPrice; }
    public BigDecimal getCurrentPrice()  { return currentPrice; }
    public BigDecimal getMinIncrement()  { return minIncrement; }
    public BigDecimal getBuyNowPrice()   { return buyNowPrice; }
    public AuctionStatus getStatus()     { return status; }
    public LocalDateTime getStartTime()  { return startTime; }
    public LocalDateTime getEndTime()    { return endTime; }
    public Long getLeaderId()            { return leaderId; }
    public String getLeaderName()        { return leaderName; }
    public int getBidCount()             { return bidCount; }

    // ─── SETTERS ──────────────────────────────────────────────────────────────

    public void setId(Long id)                       { this.id = id; }
    public void setItemId(Long itemId)               { this.itemId = itemId; }
    public void setItemName(String itemName)         { this.itemName = itemName; }
    public void setItemDescription(String desc)      { this.itemDescription = desc; }
    public void setItemCategory(String category)     { this.itemCategory = category; }
    public void setItemImageUrl(String url)          { this.itemImageUrl = url; }
    public void setSellerId(Long sellerId)           { this.sellerId = sellerId; }
    public void setSellerName(String sellerName)     { this.sellerName = sellerName; }
    public void setStartPrice(BigDecimal price)      { this.startPrice = price; }
    public void setCurrentPrice(BigDecimal price)    { this.currentPrice = price; }
    public void setMinIncrement(BigDecimal incr)     { this.minIncrement = incr; }
    public void setBuyNowPrice(BigDecimal price)     { this.buyNowPrice = price; }
    public void setStatus(AuctionStatus status)      { this.status = status; }
    public void setStartTime(LocalDateTime t)        { this.startTime = t; }
    public void setEndTime(LocalDateTime t)          { this.endTime = t; }
    public void setLeaderId(Long leaderId)           { this.leaderId = leaderId; }
    public void setLeaderName(String leaderName)     { this.leaderName = leaderName; }
    public void setBidCount(int bidCount)            { this.bidCount = bidCount; }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    /** Giá hiển thị: currentPrice nếu đã có bid, ngược lại dùng startPrice */
    public BigDecimal getDisplayPrice() {
        return (currentPrice != null) ? currentPrice : startPrice;
    }

    /** Phiên có đang nhận bid không */
    public boolean isOpen() {
        return status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING;
    }

    @Override
    public String toString() {
        return String.format(
                "AuctionDTO[id=%d, item=%s, price=%s, status=%s, leader=%s, bids=%d]",
                id, itemName, getDisplayPrice(), status, leaderName, bidCount);
    }
}