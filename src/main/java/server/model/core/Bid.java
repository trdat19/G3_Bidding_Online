package server.model.core;

import server.model.user.Bidder;
import server.model.user.User;

import java.time.LocalDateTime;

public class Bid { //thông tin 1 lần đặt giá
    private Bidder bidder;
    private double amount;
    private LocalDateTime timestamp;

    public Bid(User bidder, double amount) {
        if (!(bidder instanceof Bidder)) {
            throw new IllegalArgumentException("Only Bidder is allowed!");
        }

        this.bidder = (Bidder) bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }

    public Long getBidderId() {
        return bidder.getId();
    }

    public boolean isHigherThan(Bid other) {
        if (this.amount == other.amount) {
            return (this.timestamp.isBefore(other.timestamp));
        }
        return (this.amount > other.amount);
    }
}
