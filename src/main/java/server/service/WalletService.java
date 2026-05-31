package server.service;

import server.dao.AuctionDAO;
import server.dao.BidDAO;
import server.dao.UserDAO;
import server.model.core.Auction;
import server.model.core.Bid;
import shared.dto.common.SellerWalletDTO;
import shared.enums.AuctionStatus;
import shared.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.List;

public class WalletService {
    private static volatile WalletService instance;

    private final UserDAO userDAO = new UserDAO();
    private final BidDAO bidDAO = new BidDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    private WalletService() {}

    public static WalletService getInstance() {
        if (instance == null) {
            synchronized (WalletService.class) {
                if (instance == null) {
                    instance = new WalletService();
                }
            }
        }
        return instance;
    }

    public BigDecimal getBalance(Long userId) {
        return userDAO.getBalance(userId);
    }

    public BigDecimal deposit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0!");
        }

        boolean ok = userDAO.increaseBalance(userId, amount);
        if (!ok) {
            throw new RuntimeException("Không thể nạp tiền");
        }

        return userDAO.getBalance(userId);
    }

    public BigDecimal getAvailableBalance(Long userId, Long auctionId) {
        BigDecimal balance = userDAO.getBalance(userId);
        BigDecimal reserved = bidDAO.getReservedAmountByBidderIdExcludingAuction(
                userId, auctionId
        );

        return balance.subtract(reserved).max(BigDecimal.ZERO);
    }

    public void checkCanBid(Long userId, Long auctionId, BigDecimal amount) {
        BigDecimal availableBalance = getAvailableBalance(userId, auctionId);

        if (availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(availableBalance);
        }
    }

    public BigDecimal payForWinningBid(Long userId, BigDecimal amount) {
        boolean ok = userDAO.decreaseBalanceIfEnough(userId, amount);

        if (!ok) {
            throw new InsufficientBalanceException(userDAO.getBalance(userId));
        }

        return userDAO.getBalance(userId);
    }

    public BigDecimal addSellerRevenue(Long sellerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền cộng cho seller phải lớn hơn 0");
        }

        boolean ok = userDAO.increaseBalance(sellerId, amount);

        if (!ok) {
            throw new RuntimeException("Không thể cộng tiền cho seller");
        }

        return userDAO.getBalance(sellerId);
    }

    public SellerWalletDTO getSellerWalletSummary(Long sellerId) {
        BigDecimal balance = userDAO.getBalance(sellerId);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long soldProductCount = 0;
        List<Auction> sellerAuctions = auctionDAO.getAllAuctionsBySellerId(sellerId);

        for (Auction auction : sellerAuctions) {
            if (auction.getStatus() != AuctionStatus.FINISHED) {
                continue;
            }

            Bid highestBid = bidDAO.getHighestBidByAuctionId(auction.getId());
            if (highestBid == null || highestBid.getAmount() == null) {
                continue;
            }

            totalRevenue = totalRevenue.add(highestBid.getAmount());
            soldProductCount++;
        }

        return new SellerWalletDTO(balance, totalRevenue, soldProductCount);
    }

    public SellerWalletDTO withdrawSellerWallet(Long sellerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0.");
        }

        if (!userDAO.decreaseBalanceIfEnough(sellerId, amount)) {
            throw new IllegalArgumentException("Số dư không đủ để rút số tiền này.");
        }

        return getSellerWalletSummary(sellerId);
    }

    public void payWinnerToSeller(Long bidderId, Long sellerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }

        boolean ok = userDAO.transferBalanceIfEnough(bidderId, sellerId, amount);

        if (!ok) {
            throw new InsufficientBalanceException(userDAO.getBalance(bidderId));
        }
    }
}
