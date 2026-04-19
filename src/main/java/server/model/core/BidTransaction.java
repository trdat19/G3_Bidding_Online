package server.model.core;

public class BidTransaction {
    private final Bid bid;

    public BidTransaction(Bid bid) {
        this.bid = bid;
    }

    public void execute(Auction auction) {
        //kiểm tra tính hợp lệ của bid
        //ok thì cho vào auction
        auction.add(bid);
    }
}
