package server.model.core;

import shared.enums.AuctionStatus;

import java.time.*;
import java.util.*;

public class Auction { //state 1 phiên đấu giá có cái gì
    private final String id;
    private String itemId;
    private String sellerId;
    private double startPrice;
    private AuctionStatus status;
    private LocalDateTime startTime, endTime;
    private List<Bid> bids;
    private Bid highestBid;

    public Auction(String id, String itemId, String sellerId, double startPrice, LocalDateTime startTime, LocalDateTime endTime) {
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
//        status = AuctionStatus.FINISHED;
    }
//    public boolean isRunning() {
//        return (status == AuctionStatus.RUNNING);
//    }

    public void add(Bid bid) {
        bids.add(bid);
    }
}
