package server.model.core;

import server.model.Entity;
import shared.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Auction extends Entity { //state 1 phiên đấu giá có cái gì

    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long sellerId;
    private BigDecimal startPrice;
    private BigDecimal maxPrice;
    private BigDecimal minIncrement;
    private BigDecimal buyNowPrice;
    private AuctionStatus status;
    private LocalDateTime startTime, endTime;
    private List<Bid> bids;
    private Bid highestBid;

    public Auction() {}

    //tạo mới
    public Auction(Long itemId, Long sellerId, BigDecimal startPrice,
                   BigDecimal maxPrice, BigDecimal minIncrement, BigDecimal buyNowPrice,
                   LocalDateTime startTime, LocalDateTime endTime)
    {
        super();
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startPrice = startPrice;
        this.maxPrice = maxPrice;
        this.minIncrement = minIncrement;
        this.buyNowPrice = buyNowPrice;
        this.status = AuctionStatus.PREPARING;
        this.startTime = startTime;
        this.endTime = endTime;
        bids = new ArrayList<>();
        highestBid = null;
    }

    public void open() {
        status = AuctionStatus.OPEN;
    }
    public void close() {
        status = AuctionStatus.CLOSED;
    }
    public boolean isRunning() {
        return (status == AuctionStatus.OPEN);
    }

    public void add(Bid bid) {
        bids.add(bid);

        if (highestBid == null || bid.isHigherThan(highestBid)) {
            highestBid = bid;
        }
    }

    //getter
    public Long getItemId() { return itemId; }
    public Long getSellerId() { return sellerId; }
    public BigDecimal getStartPrice() { return startPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public BigDecimal getMinIncrement() { return minIncrement; }
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public AuctionStatus getStatus() { return status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<Bid> getAllBids() {
        return new ArrayList<>(bids);
    }
    public Bid getHighestBid() {
        return highestBid;
    }

    //setter
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public void setMinIncrement(BigDecimal minIncrement) {
        this.minIncrement = minIncrement;
    }
    public void setBuyNowPrice(BigDecimal buyNowPrice) {
        this.buyNowPrice = buyNowPrice ;
    }
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String getInfo() {
        return String.format(
                "[Auction %d] Item: %d | Current: %s | Status: %s",
                id, itemId, maxPrice, status);
    }

}
