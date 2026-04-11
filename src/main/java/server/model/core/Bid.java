package server.model.core;

import server.model.user.Bidder;
import server.model.user.User;

import java.time.LocalDateTime;

public class Bid {
    private Bidder bidder;
    private double bidAmount;
    private LocalDateTime timestamp;

    public Bid(User bidder, double bidAmount) {
        if (bidder == null || bidder.getClass() != Bidder.class) {
            throw new IllegalArgumentException("Only Bidder is allowed!");
        }

        this.bidder = new Bidder(bidder.getUsername(), bidder.getPassword());
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public Bidder getBidder() {
        return new Bidder(bidder.getUsername(), bidder.getPassword());
    }

//    @Override
//    public boolean compareTo(Bid otherBid) {
//        return this.bidAmount > otherBid.bidAmount;
//    }
}
