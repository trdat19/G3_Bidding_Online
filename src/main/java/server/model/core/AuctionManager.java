package server.model.core;

import server.model.user.Bidder;

import java.util.*;

//Singleton
public class AuctionManager {
    private static volatile AuctionManager instance = null;
    private Map<String, Auction> auctions;

    private AuctionManager() {
        auctions = new HashMap<>();
    }

    public static AuctionManager getInstance() {
        if (instance == null) { //lazy init
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    public void createAuction() {}
    public void closeAuction() {}
    public void removeAuction(String id) {
        auctions.remove(id);
    }

    public Auction getAuction(String id) {
        return auctions.get(id);
    }
    public List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    public void placeBid(Long bidderId, String auctionId, double amount) {
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            throw new IllegalArgumentException("Auction not found!");
        }

        //BidTransaction transaction = new BidTransaction(new Bid(bidderId, amount, ));

    }
}
