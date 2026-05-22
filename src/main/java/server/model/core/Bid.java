package server.model.core;

import server.model.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bid extends Entity { //thông tin 1 lần đặt giá

    private static final long serialVersionUID = 1L;

    private Long auctionId;
    private Long bidderId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private Boolean isAutoBid;

    public Bid() {}

    public Bid(Long auctionId, Long bidderId, BigDecimal amount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.isAutoBid = false;
    }

    //getter
    public Long getAuctionId() {
        return auctionId;
    }
    public Long getBidderId() {
        return bidderId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public Boolean getIsAutoBid(){ return isAutoBid;}

    //setter
    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }
    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setIsAutoBid(Boolean isAutoBid) {this.isAutoBid = isAutoBid; }

    @Override
    public String getInfo() {
        return String.format("Bid[%s] by %d at %s",//%s",
                amount, bidderId, timestamp /*isAutoBid ? " [AUTO]" : ""*/);
    }

    public boolean isHigherThan(Bid other) {
        int cmp = this.amount.compareTo(other.amount);

        if (cmp > 0) return true;
        if (cmp < 0) return false;

        // bằng tiền → so thời gian
        return this.timestamp.isAfter(other.timestamp);
    }


}
