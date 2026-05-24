package server.service;

import server.dao.AuctionDAO;
import server.dao.AutoBidRuleDAO;
import server.dao.BidDAO;
import server.model.core.Auction;
import server.model.core.AutoBidRule;
import server.model.core.Bid;
import shared.exception.AuctionNotFoundException;
import shared.exception.InvalidBidException;

import java.math.BigDecimal;
import java.util.List;

public class AutoBidService {
    private static AutoBidService instance;

    private final AutoBidRuleDAO autoBidRuleDAO = new AutoBidRuleDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidDAO bidDAO = new BidDAO();

    private final BidService bidService = BidService.getInstance();

    private AutoBidService() {}

    public static AutoBidService getInstance() {
        if (instance == null) {
            synchronized (AutoBidService.class) {
                if (instance == null) {
                    instance = new AutoBidService();
                }
            }
        }
        return instance;
    }


    public boolean registerAutoBidRule(Long auctionId, Long bidderId,
                                       BigDecimal maxAmount, BigDecimal stepAmount) {

        if (auctionId == null || bidderId == null || maxAmount == null) {
            throw new IllegalArgumentException("Thiếu thông tin auto bid");
        }

        // 1. Kiểm tra
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException(auctionId);
        }

        if (maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Max amount không hợp lệ!");
        }

        if (stepAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Step amount không hợp lệ!");
        }

        Bid highestBid = bidDAO.getHighestBidByAuctionId(auctionId);
        BigDecimal currentPrice = highestBid != null
                ? highestBid.getAmount()
                : auction.getStartPrice();

        BigDecimal minRequired = currentPrice.add(auction.getMinIncrement());
        if (maxAmount.compareTo(minRequired) < 0) {
            throw new InvalidBidException("Max amount phải >= " + minRequired);
        }
        if (stepAmount.compareTo(auction.getMinIncrement()) < 0) {
            throw new InvalidBidException("Step amount phải >= " + auction.getMinIncrement());
        }

        AutoBidRule rule = new AutoBidRule(auctionId, bidderId, maxAmount, stepAmount);
        return autoBidRuleDAO.saveOrUpdateRule(rule);
    }

    public void removeAutoBidRule() {

    }

    /** phương thức chính - các autobid đấu giá qua lại đến khi chạm ngưỡng điều kiện */
    public synchronized void reactToIncomingBid(Long auctionId, Long currentBidderId) {
        while (true) {
            Bid highestBid = bidDAO.getHighestBidByAuctionId(auctionId);
            if (highestBid == null) {
                return;
            }

            BigDecimal currentPrice = highestBid.getAmount();
            Long highestBidderId = highestBid.getBidderId();

            List<AutoBidRule> rules =
                    autoBidRuleDAO.getEligibleRules(auctionId, highestBidderId, currentPrice);

            if (rules.isEmpty()) {
                return;
            }

            Auction auction = auctionDAO.findById(auctionId);
            if (auction == null) {
                return;
            }

            AutoBidRule rule = rules.get(0);

            BigDecimal stepAmount = rule.getStepAmount() != null ? rule.getStepAmount() : auction.getMinIncrement();

            BigDecimal increment = stepAmount.max(auction.getMinIncrement());
            BigDecimal nextAmount = currentPrice.add(increment).compareTo(rule.getMaxAmount()) > 0
                                    ? rule.getMaxAmount()
                                    : currentPrice.add(increment);

            if (nextAmount.compareTo(currentPrice) <= 0) {
                autoBidRuleDAO.updateStatus(rule.getId(), false);
                return;
            }

            Bid autoBid = new Bid(auctionId, rule.getBidderId(), nextAmount);
            autoBid.setIsAutoBid(true);

            boolean placed = BidService.getInstance().placeAutoBid(
                    auctionId,
                    rule.getBidderId(),
                    nextAmount
            );

            if (!placed) {
                return;
            }
        }
    }

    /**
     * Bật/tắt auto bid của bidder trong auction.
     */
    public boolean switchAutoBid(Long auctionId, Long bidderId, boolean active) {
        if (auctionId == null || bidderId == null) {
            throw new IllegalArgumentException("Thiếu auctionId hoặc bidderId");
        }

        return autoBidRuleDAO.switchActiveStateRule(auctionId, bidderId, active);
    }

    //----------GET/SET---------------
    public AutoBidRule getRule(Long auctionId, Long bidderId) {
        return autoBidRuleDAO.findByAuctionIdAndBidderId(auctionId, bidderId);
    }
}
