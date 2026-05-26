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
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.spi.ToolProvider;

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
        boolean saved = autoBidRuleDAO.saveOrUpdateRule(rule);
        if (saved) {
            Bid highest = bidDAO.getHighestBidByAuctionId(auctionId);
            //nếu thằng đặt rule vẫn đang dẫn đầu thì không tự react
            if (highest != null && !highest.getBidderId().equals(bidderId)) {
                reactToIncomingBidLocked(auctionId);
            }
        }
        return saved;
    }

    public void removeAutoBidRule() {

    }

    //---------------MAIN_METHOD-------------
    /** phương thức chính - các autobid đấu giá qua lại, phản ứng với nhau đến khi chạm ngưỡng điều kiện
     * sử dụng PriorityQueue cho các điều kiện
     * 1. maxAmount cao hơn
     * 2. created_at sớm hơn
     * 3. id nhỏ hơn
     * Không insert tất cả các auto bid vào DB, chỉ insert bid cuối mạnh nhất của người thắng
     */

    /** method có lấy lock để gọi method chính */
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

        BigDecimal currentPrice = highestBid.getAmount();
        Long highestBidderId = highestBid.getBidderId();

        BigDecimal minIncrement = auction.getMinIncrement() != null
                ? auction.getMinIncrement()
                : BigDecimal.ZERO;

        BigDecimal minNextBid = currentPrice.add(minIncrement);

        PriorityQueue<AutoBidRule> queue = buildAutoBidQueue(auctionId);

        AutoBidDecision decision = calculateDecision(
                queue,
                highestBidderId,
                currentPrice,
                minIncrement,
                minNextBid
        );

        if (decision == null) {
            return;
        }

        boolean placed = BidService.getInstance().placeAutoBid(
                auctionId,
                decision.getBidderId(),
                decision.getAmount()
        );

        if (!placed) {
            return;
        }
    }

    //----------------HELPER_CLASS-----------------
    private static class AutoBidDecision {
        private final Long bidderId;
        private final BigDecimal amount;

        private AutoBidDecision(Long bidderId, BigDecimal amount) {
            this.bidderId = bidderId;
            this.amount = amount;
        }

        public Long getBidderId() { return bidderId; }

        public BigDecimal getAmount() { return amount; }
    }
    //-------------------HELPER-----------------------
    /**
     * Tính kết quả cuối của AutoBid mà không insert từng bước.
     *
     * Ý tưởng:
     * - topRule: rule mạnh nhất trong PriorityQueue.
     * - secondRule: rule mạnh thứ hai.
     *
     * Nếu topRule thuộc người đang dẫn đầu: -> đã có rule mạnh nhất -> không cần tự bid.
     *
     * Nếu topRule mạnh hơn secondRule: -> topRule chỉ cần đặt giá vượt secondRule.
     *
     * Nếu topRule và secondRule cùng maxAmount:
     *      topRule thắng nhờ createdAt sớm hơn/id nhỏ hơn,
     *      nhưng phải đặt tới đúng maxAmount.
     */
    private AutoBidDecision calculateDecision(
            PriorityQueue<AutoBidRule> queue,
            Long highestBidderId,
            BigDecimal currentPrice,
            BigDecimal minIncrement,
            BigDecimal minNextBid) {

        AutoBidRule topRule = takeNextUsableRule(queue, minNextBid);
        if (topRule == null) {
            return null;
        }

        // Nếu người đang dẫn đầu cũng là người có rule mạnh nhất,
        // không cần tự động đặt thêm giá cho chính mình.
        if (topRule.getBidderId().equals(highestBidderId)) {
            return null;
        }

        AutoBidRule secondRule = takeNextDifferentBidderRule(
                queue,
                topRule.getBidderId(),
                minNextBid
        );

        BigDecimal finalAmount = calculateAmount(
                topRule,
                secondRule,
                currentPrice,
                minIncrement,
                minNextBid
        );
        if (finalAmount == null) {
            return null;
        }

        // Nếu không amount k đạt yêu cầu mới bid tối thiểu tiếp theo
        if (finalAmount.compareTo(minNextBid) < 0) {
            autoBidRuleDAO.updateStatus(topRule.getId(), false);
            return null;
        }

        return new AutoBidDecision(topRule.getBidderId(), finalAmount);
    }
    /**
     * Lấy rule tiếp theo có maxAmount đủ để đặt giá tối thiểu.
     */
    private AutoBidRule takeNextUsableRule(
            PriorityQueue<AutoBidRule> queue,
            BigDecimal minNextBid
    ) {
        while (!queue.isEmpty()) {
            AutoBidRule rule = queue.poll();

            if (rule.getMaxAmount().compareTo(minNextBid) < 0) {
                autoBidRuleDAO.updateStatus(rule.getId(), false);
                continue;
            }

            return rule;
        }

        return null;
    }
    /**
     * Lấy rule mạnh thứ hai, khác bidder với topRule.
     */
    private AutoBidRule takeNextDifferentBidderRule(
            PriorityQueue<AutoBidRule> queue,
            Long topBidderId,
            BigDecimal minNextBid
    ) {
        while (!queue.isEmpty()) {
            AutoBidRule rule = queue.poll();

            if (rule.getBidderId().equals(topBidderId)) {
                continue;
            }

            if (rule.getMaxAmount().compareTo(minNextBid) < 0) {
                autoBidRuleDAO.updateStatus(rule.getId(), false);
                continue;
            }

            return rule;
        }

        return null;
    }

    /**
     * Tính số tiền auto bid cuối mạnh nhất sẽ insert.
     */
    private BigDecimal calculateAmount(
            AutoBidRule topRule,
            AutoBidRule secondRule,
            BigDecimal currentPrice,
            BigDecimal minIncrement,
            BigDecimal minNextBid) {

        BigDecimal expectedAmount = calculateExpectedAmount(topRule, secondRule,
                                                            currentPrice, minIncrement);

        if (expectedAmount == null) {
            return null;
        }

        // Dù tính kiểu gì thì bid mới vẫn phải >= currentPrice + minIncrement.
        if (expectedAmount.compareTo(minNextBid) < 0) {
            return minNextBid;
        }

        // Không bao giờ vượt quá maxAmount của topRule.
        if (expectedAmount.compareTo(topRule.getMaxAmount()) > 0) {
            return topRule.getMaxAmount();
        }

        return expectedAmount;
    }

    private BigDecimal calculateExpectedAmount(AutoBidRule topRule, AutoBidRule secondRule,
                                               BigDecimal currentPrice, BigDecimal minIncrement) {
        BigDecimal topStep = topRule.getStepAmount() != null
                ? topRule.getStepAmount()
                : minIncrement;

        BigDecimal actualStep = topStep.max(minIncrement);

        if (secondRule == null) {
            // Chỉ có 1 auto rule đủ điều kiện
            // đặt giá hiện tại + step
            return currentPrice.add(actualStep);
        }

        int compareMax = topRule.getMaxAmount().compareTo(secondRule.getMaxAmount());

        if (compareMax > 0) {
            // topRule mạnh hơn secondRule:
            // chỉ cần vượt max của secondRule.
            return secondRule.getMaxAmount().add(actualStep);
        }

        if (compareMax == 0) {
            // Cùng maxAmount:
            // topRule thắng nhờ createdAt sớm hơn/id nhỏ hơn,
            // nhưng phải lên tới đúng maxAmount.
            return topRule.getMaxAmount();
        }

        // Trường hợp còn lại gần như không xảy ra vì PriorityQueue đã xếp topRule theo created_at
            return null;
    }

    private PriorityQueue<AutoBidRule> buildAutoBidQueue(Long auctionId) {
        Comparator<AutoBidRule> comparator = Comparator
                .comparing(AutoBidRule::getMaxAmount, Comparator.reverseOrder())
                .thenComparing(AutoBidRule::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AutoBidRule::getId,
                        Comparator.nullsLast(Comparator.naturalOrder()));

        PriorityQueue<AutoBidRule> queue = new PriorityQueue<>(comparator);

        queue.addAll(autoBidRuleDAO.getActiveRulesByAuctionId(auctionId));

        return queue;
    }

    //----------------SWITCH----------------
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

