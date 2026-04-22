package server.model.core;

import server.model.user.Bidder;
import server.model.user.User;

import java.sql.Time;
import java.sql.Timestamp;

public class Bid { //thông tin 1 lần đặt giá
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private double amount;
    private Timestamp timestamp;

    public Bid(Long id, Long bidderId, double amount) {
        this.id = id;
        this.bidderId = bidderId;
        this.amount = amount;
    }

//    //dùng để tạo thông tin cho BidTransaction khi Bidder đặt lệnh Bid
//    public Bid(Long bidderId, double amount, Timestamp timestamp) {
//        this.bidderId = bidderId;
//        this.amount = amount;
//        this.timestamp = timestamp;
//    }

    //getter
    public Long getId() {
        return id;
    }
    public Long getAuctionId() {
        return auctionId;
    }
    public Long getBidderId() {
        return bidderId;
    }
    public double getAmount() {
        return amount;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }

    //setter
    public void setId(Long id) {
        this.id = id;
    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }
    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
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
