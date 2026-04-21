package server.model.core;

import shared.enums.AuctionStatus;

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
}
