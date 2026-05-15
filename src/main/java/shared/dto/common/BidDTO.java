package shared.dto.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BidDTO - dữ liệu đặt giá truyền giữa client và server.
 *
 * Chứa thông tin về một lượt đặt giá: ai đặt, giá bao nhiêu, lúc nào, thuộc phiên đấu giá nào.
 * Dùng để hiển thị lịch sử đặt giá, thông tin đặt giá mới, v.v.
 */
public class BidDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long auctionId;
    private Long bidderId;
    private String bidderName;   // Tên hiển thị
    private BigDecimal amount;
    private LocalDateTime timestamp;

    //-------------------CONSTRUCTOR-----------
    public BidDTO() {}

    /** Constructor đầy đủ */
    public BidDTO(Long id, Long auctionId, Long bidderId, String bidderName,
                  BigDecimal amount, LocalDateTime timestamp) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // Getters
    public Long getId() { return id; }
    public Long getAuctionId() { return auctionId; }
    public Long getBidderId() { return bidderId; }
    public String getBidderName() { return bidderName; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }
    public void setBidderId(Long bidderId) { this.bidderId = bidderId; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("BidDTO[auction=%d, bidder=%s, amount=%s, time=%s]",
                auctionId, bidderName, amount, timestamp);
    }
}