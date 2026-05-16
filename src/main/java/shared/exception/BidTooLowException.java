package shared.exception;

import java.math.BigDecimal;

public class BidTooLowException extends AuctionException {

    public BidTooLowException(BigDecimal minBid) {
        super("Bid phải >= " + minBid);
    }
}
