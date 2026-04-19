package server.model.core;

import java.time.LocalDateTime;

public class BidTransaction {
    private final String id;
    private String auctionId;
    private String bidderId;
    private double amount;
    private LocalDateTime timestamp;

    public BidTransaction(String id, String auctionId, String bidderId) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
    }

    public boolean isHigherThan(BidTransaction other) {
        if (this.amount == other.amount) {
            return (this.timestamp.isBefore(other.timestamp));
        }
        return (this.amount > other.amount);
    }
}
