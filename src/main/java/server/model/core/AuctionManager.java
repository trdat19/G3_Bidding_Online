package server.model.core;

import server.dao.BidDAO;
import server.dao.UserDAO;
import server.model.user.Bidder;
import server.model.user.User;
import shared.enums.AuctionStatus;
import shared.exception.AuctionClosedException;
import shared.exception.BidTooLowException;
import shared.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

//Singleton
public class AuctionManager {
    private static volatile AuctionManager instance = null;
    private Map<Long, Auction> auctions;

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
    public void closeAuction(Long auctionId) {
        Auction auction = auctions.get(auctionId);
        auction.setStatus(AuctionStatus.CLOSED);
    }
    public void removeAuction(Long id) {
        auctions.remove(id);
    }

    public Auction getAuction(Long id) {
        return auctions.get(id);
    }
    public List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    public void placeBid(Long bidderId, Long auctionId, BigDecimal amount) {
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            throw new IllegalArgumentException("Không tìm thấy Auction!");
        }

        if (!auction.isRunning()) {
            throw new AuctionClosedException();
        }

        Bid currentHighestBid = auction.getHighestBid();
        BigDecimal minBid;
        if (currentHighestBid == null) {
            minBid = auction.getStartPrice();
        }
        else
            minBid = currentHighestBid.getAmount().add(auction.getMinIncrement());

        if (amount.compareTo(minBid) < 0) {
            throw new BidTooLowException(minBid);
        }

        UserDAO userDao = new UserDAO();
        User user = userDao.findById(bidderId);

        if (!(user instanceof Bidder)) {
            throw new IllegalArgumentException("bidderId truyền vào không phải là Bidder");
        }

        user = (Bidder) user;
        if (!((Bidder) user).canAfford(amount)) {
            throw new InsufficientBalanceException(((Bidder) user).getBalance());
        }

        BidDAO bidDao = new BidDAO();
        Bid bid = new Bid(auctionId, bidderId, amount);
        bid.setTimestamp(LocalDateTime.now());

        BidTransaction transaction = new BidTransaction(bid);
        transaction.execute(auction);


    }
}
