package server.model.core;

import shared.enums.AuctionStatus;

import java.sql.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

public class Auction { //state 1 phiên đấu giá có cái gì
    private Long id;
    private Long itemId;
    private Long sellerId;
    private double startPrice;
    private double max_price;
    private double min_increment;
    private double buy_now_price;
    private AuctionStatus status;
    private Timestamp startTime, endTime;
    private List<Bid> bids;
    private Bid highestBid;

    public Auction(Long id, Long itemId, Long sellerId, double startPrice, Timestamp startTime, Timestamp endTime) {
        this.id = id;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startPrice = startPrice;
        status = AuctionStatus.OPEN;
        this.startTime = startTime;
        this.endTime = endTime;
        bids = new ArrayList<>();
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
    }

    //getter
    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public Long getSellerId() { return sellerId; }
    public double getStartPrice() { return startPrice; }
    public double getMax_price() { return max_price; }
    public double getMin_increment() { return min_increment; }
    public double getBuy_now_price() { return buy_now_price; }
    public AuctionStatus getStatus() { return status; }
    public Timestamp getStartTime() { return startTime; }
    public Timestamp getEndTime() { return endTime; }
    public List<Bid> getBids() {
        List<Bid> bid_list = new ArrayList<>();
        for (Bid bid : bids) {
            bid_list.add(bid);
        }
        return bid_list;
    }
    public Bid getHighestBid() { return highestBid; }

    //setter
    public void setStartPrice(double price) {
        this.startPrice = startPrice;
    }
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    public void setMax_price(double max_price) {
        this.max_price = max_price;
    }
    public void setMin_increment(double min_increment) {
        this.min_increment = min_increment;
    }
    public void set

}
