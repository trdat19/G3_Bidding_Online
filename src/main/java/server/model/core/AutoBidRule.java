package server.model.core;

import server.model.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutoBidRule extends Entity {

    private Long auctionId;
    private Long bidderId;
    private BigDecimal maxAmount;
    private BigDecimal stepAmount;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AutoBidRule() {}

    public AutoBidRule(Long auctionId, Long bidderId, BigDecimal maxAmount, BigDecimal stepAmount) {
        super();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxAmount = maxAmount;
        this.stepAmount = stepAmount;
        this.isActive = true;
    }

    //getter
    public Long getAuctionId() { return auctionId; }
    public Long getBidderId() { return bidderId; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public BigDecimal getStepAmount() { return stepAmount; }
    public boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    //setter
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }
    public void setBidderId(Long bidderId) { this.bidderId = bidderId; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public void setStepAmount(BigDecimal stepAmount) { this.stepAmount = stepAmount; }
    public void setIsActive(boolean state) { isActive = state; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String getInfo() {
        return String.format("AutoBidRule{id=%d, auctionId=%d, bidderId=%d, maxAmount=%s, stepAmount=%s, isActive=%s}",
                id, auctionId, bidderId, maxAmount.toPlainString(), stepAmount.toPlainString(), isActive);
    }
}
