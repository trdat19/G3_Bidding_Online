package server.model.core;

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

    public Auction getAuction(String id) {
        return auctions.get(id);
    }
    public List<Auction> getAllAuctions() {
        return (ArrayList) auctions.values();
    }

    public void removeAuction(String id) {
        auctions.remove(id);
    }

}
