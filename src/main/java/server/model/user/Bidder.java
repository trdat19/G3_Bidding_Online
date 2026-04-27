package server.model.user;

import server.model.core.AuctionManager;
import shared.enums.UserRole;

import java.math.BigDecimal;

public class Bidder extends User {

    private static final long serialVersionUID = 1L;

    private BigDecimal balance;
    private BigDecimal maxBid; // trong phần auto-bid
    private BigDecimal bidIncrement;  // trong phần auto - bid;

    public Bidder() {}

    public Bidder(String username, String password, String fullName, String email) {
        super(username, password, fullName, email);
        this.role = UserRole.BIDDER;
        balance = new BigDecimal(0);
        maxBid = new BigDecimal(0);
        bidIncrement = new BigDecimal(0);
    }

    //getter
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getMaxBid() { return maxBid; }
    public BigDecimal getBidIncrement() { return bidIncrement; }

    //setter
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setMaxBid(BigDecimal maxBid) { this.maxBid = maxBid; }
    public void setBidIncrement(BigDecimal increment) { this.bidIncrement = increment; }

    @Override
    public String getInfo() {
        return super.getInfo() + String.format(" | Balance: %s", balance);
    }

    public void placeBid(String auctionId, double amount) {
        AuctionManager manager = AuctionManager.getInstance();

        // Bid bid = new Bid() tạo bid mới để đặt giá

    }


}
