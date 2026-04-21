package server.model.core;

import server.model.user.Bidder;
import server.model.user.User;

import java.sql.Timestamp;

public class Bid { //thông tin 1 lần đặt giá
    private Long auctionId;
    private Long bidderId;
    private double amount;
    private Timestamp timestamp;

    public Bid(Long bidderId, double amount) {
        this.bidderId = bidderId;
        this.amount = amount;
    }

    //dùng để tạo thông tin cho BidTransaction khi Bidder đặt lệnh Bid
    public Bid(Long bidderId, double amount, Timestamp timestamp) {
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    
    public double getAmount() {
        return amount;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public boolean isHigherThan(Bid other) {
        if (this.amount == other.amount) {
            int com = this.timestamp.compareTo(other.timestamp);
            if (com <= 0)
                return false;
        }
        return (this.amount > other.amount);
    }
}
