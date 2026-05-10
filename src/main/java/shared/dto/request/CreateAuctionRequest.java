package shared.dto.request;

import server.model.core.Bid;
import shared.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CreateAuctionRequest {

    private Long itemId;
    private Long sellerId;
    private BigDecimal startPrice;
    private BigDecimal minIncrement;
    private BigDecimal buyNowPrice;
    private LocalDateTime startTime, endTime;

    //GETTER
    public Long getItemId() { return itemId; }
    public Long getSellerId() { return sellerId; }
    public BigDecimal getStartPrice() { return startPrice; }
    public BigDecimal getMinIncrement() { return minIncrement; }
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    //SETTER
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }
    public void setMinIncrement(BigDecimal minIncrement) {
        this.minIncrement = minIncrement;
    }
    public void setBuyNowPrice(BigDecimal buyNowPrice) {
        this.buyNowPrice = buyNowPrice ;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}
