package server.service;

import server.concurrency.AuctionLockManager;
import server.dao.AuctionDAO;
import server.dao.AutoBidRuleDAO;
import server.dao.BidDAO;
import server.model.core.Auction;
import server.model.core.AutoBidRule;
import server.model.core.Bid;

import shared.exception.AuctionNotFoundException;
import shared.exception.InvalidBidException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class AutoBidService {
    private static volatile AutoBidService instance;

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
        boolean saved = autoBidRuleDAO.saveOrUpdateRule(rule);
        if (saved) {
            // Cho rule tạo bid nếu ban đầu chưa có bid nào
            Bid highest = bidDAO.getHighestBidByAuctionId(auctionId);

            if (highest == null) {
                BigDecimal firstBid = auction.getStartPrice()
                        .add(stepAmount.max(auction.getMinIncrement()));

                if (firstBid.compareTo(maxAmount) > 0) {
                    firstBid = maxAmount;
                }

                BigDecimal minimumValidBid = auction.getStartPrice()
                        .add(auction.getMinIncrement());

                if (firstBid.compareTo(minimumValidBid) >= 0) {
                    try {
                        bidService.placeAutoBid(auctionId, bidderId, firstBid);
                    } catch (Exception e) {
                        AutoBidRule savedRule =
                                autoBidRuleDAO.findByAuctionIdAndBidderId(auctionId, bidderId);

                        if (savedRule != null) {
                            autoBidRuleDAO.updateStatus(savedRule.getId(), false);
                        }

                        throw e;
                    }
                }

            } else if (!highest.getBidderId().equals(bidderId)){ //nếu thg đặt rule vẫn đang dẫn đầu thì k tự react
                reactToIncomingBid(auctionId);
            }
        }
        return saved;
    }

    //---------------MAIN_METHOD-------------
    /*
    A và B tự động đấu qua lại bằng đúng stepAmount của từng người.
    */

    //lấy lock
    public void reactToIncomingBid(Long auctionId) {
        ReentrantLock lock = AuctionLockManager.getInstance().getLock(auctionId);
        lock.lock();

        try {
            reactToIncomingBidLocked(auctionId);
        } finally {
            lock.unlock();
        }
    }

    public void reactToIncomingBidLocked(Long auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            return;
        }

        Bid highestBid = bidDAO.getHighestBidByAuctionId(auctionId);
        if (highestBid == null) {
            return;
        }

        BigDecimal minIncrement = auction.getMinIncrement() != null ? auction.getMinIncrement() : BigDecimal.ONE;
        BigDecimal currentPrice = highestBid.getAmount();
        Long currentLeaderId = highestBid.getBidderId();

        while (true) {
            AutoBidRule challenger = findNextChallenger(
                    auctionId,
                    currentLeaderId,
                    currentPrice,
                    minIncrement
            );

            if (challenger == null) {
                return;
            }

            BigDecimal nextAmount = calculateNextAutoBidAmount(
                    challenger,
                    currentPrice,
                    minIncrement
            );

            if (nextAmount == null) {
                autoBidRuleDAO.updateStatus(challenger.getId(), false);
                continue;
            }

            try {
                boolean placed = BidService.getInstance().placeAutoBid(
                        auctionId,
                        challenger.getBidderId(),
                        nextAmount
                );

                if (!placed) {
                    autoBidRuleDAO.updateStatus(challenger.getId(), false);
                    continue;
                }
            } catch (Exception e) {
                autoBidRuleDAO.updateStatus(challenger.getId(), false);
                continue;
            }

            currentPrice = nextAmount;
            currentLeaderId = challenger.getBidderId();
        }
    }

    /*
    • Bình thường tăng theo stepAmount.
    • Nếu bước tăng vượt max nhưng bidder vẫn còn đủ để đặt một giá hợp lệ, đặt đúng maxAmount.
    • Nếu max còn nhỏ hơn giá tối thiểu hợp lệ, bidder không thể tiếp tục.
     */
    private BigDecimal calculateNextAutoBidAmount(
            AutoBidRule rule,
            BigDecimal currentPrice,
            BigDecimal minIncrement) {

        BigDecimal step = rule.getStepAmount().max(minIncrement);
        BigDecimal nextAmount = currentPrice.add(step);

        if (nextAmount.compareTo(rule.getMaxAmount()) <= 0) {
            return nextAmount;
        }

        BigDecimal minimumValidBid = currentPrice.add(minIncrement);

        if (rule.getMaxAmount().compareTo(minimumValidBid) >= 0) {
            return rule.getMaxAmount();
        }

        return null;
    }

    private AutoBidRule findNextChallenger(
            Long auctionId,
            Long currentLeaderId,
            BigDecimal currentPrice,
            BigDecimal minIncrement) {

        BigDecimal minimumValidBid = currentPrice.add(minIncrement);

        List<AutoBidRule> activeRules =
                autoBidRuleDAO.getActiveRulesByAuctionId(auctionId);

        // build hàng đợi các rule theo quy tắc từ maxAmount -> created_at -> id
        PriorityQueue<AutoBidRule> priorityQueue = new PriorityQueue<>(
                Comparator.comparing(
                                AutoBidRule::getMaxAmount,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                AutoBidRule::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(
                                AutoBidRule::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
        );

        for (AutoBidRule rule : activeRules) {
            if (rule.getBidderId().equals(currentLeaderId)) {
                continue;
            }

            if (rule.getMaxAmount().compareTo(minimumValidBid) < 0) {
                continue;
            }

            priorityQueue.offer(rule);
        }

        return priorityQueue.poll();
    }
    //----------------SWITCH----------------
    /**
     * Bật/tắt auto bid của bidder trong auction.
     */
    public boolean switchAutoBid(Long auctionId, Long bidderId, boolean active) {
        if (auctionId == null || bidderId == null) {
            throw new IllegalArgumentException("Thiếu auctionId hoặc bidderId");
        }

        return autoBidRuleDAO.switchRuleByAuctionIdAndBidderId(auctionId, bidderId, active);
    }

    //----------GET/SET---------------
    public AutoBidRule getRule(Long auctionId, Long bidderId) {
        return autoBidRuleDAO.findByAuctionIdAndBidderId(auctionId, bidderId);
    }
}
