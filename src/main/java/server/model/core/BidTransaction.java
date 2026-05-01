package server.model.core;

import java.math.BigDecimal;

public class BidTransaction { // xử lí logic, xem Bid có hợp lệ k
    private final Bid bid;

    public BidTransaction(Bid bid) {
        this.bid = bid;
    }

    public void execute(Auction auction) {
        //kiểm tra tính hợp lệ của bid
        //ok thì cho vào auction

        //check max-price: auto close;
        BigDecimal currentMaxPrice = auction.getMaxPrice();
        if (currentMaxPrice != null &&
            bid.getAmount().compareTo(currentMaxPrice) >= 0) {
                auction.close();
        }
        auction.add(bid);
    }
}
