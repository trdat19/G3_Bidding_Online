package server.model.core;

import server.model.item.Item;
import server.model.user.Seller;
import shared.enums.AuctionStatus;

import javax.swing.plaf.ActionMapUIResource;
import java.time.*;
import java.util.*;

public class Auction {
    private final String id;
    private String itemId;
    private String sellerId;
    private double startPrice, currentPrice;
    private AuctionStatus status;
    private LocalDateTime startTime, endTime;
    private String winnerId;

    public Auction(String id, String itemId, String sellerId, double startPrice, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startPrice = startPrice;
        status = AuctionStatus.OPEN;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void open() {
        status = AuctionStatus.OPEN;
    }
    public void close() {
        status = AuctionStatus.FINISHED;
    }

    public boolean isRunning() {
        return (status == AuctionStatus.RUNNING);
    }
}
