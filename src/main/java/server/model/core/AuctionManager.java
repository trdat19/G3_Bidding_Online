package server.model.core;

import server.dao.AuctionDAO;
import shared.exception.AuctionClosedException;
import shared.exception.BidTooLowException;

import java.math.BigDecimal;

/**
 * AuctionManager quản lý runtime, schedule,... hay nói đơn giản là sẽ gọi AuctionService để xử lý logic
 */
public class AuctionManager {

    private AuctionDAO auctionDAO = new AuctionDAO();

    private static volatile AuctionManager instance = null;

    private AuctionManager() {}

    /**
     * double-check locking cho Singleton
     */
    public static AuctionManager getInstance() {
        //check lần 1
        if (instance == null) {

            synchronized (AuctionManager.class) {
                //check lần 2
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }


//    public void closeAuction(Long auctionId) {
//        Auction auction = auctions.get(auctionId);
//        auction.setStatus(AuctionStatus.CLOSED);
//    }
//    public void removeAuction(Long id) {
//        auctions.remove(id);
//    }




    public void placeBid(Long bidderId, Long auctionId, BigDecimal amount) {
        Auction auction = auctionDAO.findById(auctionId);

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

//        UserDAO userDao = new UserDAO();
//        User user = userDao.findById(bidderId);
//
//        if (!(user instanceof Bidder)) {
//            throw new IllegalArgumentException("bidderId truyền vào không phải là Bidder");
//        }
//
//        user = (Bidder) user;
//        if (!((Bidder) user).canAfford(amount)) {
//            throw new InsufficientBalanceException(((Bidder) user).getBalance());
//        }
//
//        BidDAO bidDao = new BidDAO();
//        Bid bid = new Bid(auctionId, bidderId, amount);
//        bid.setTimestamp(LocalDateTime.now());
//
//        BidTransaction transaction = new BidTransaction(bid);
//        transaction.execute(auction);


    }
}
