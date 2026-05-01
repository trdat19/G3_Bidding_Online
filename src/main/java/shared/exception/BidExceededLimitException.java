package shared.exception;

import java.math.BigDecimal;

public class BidExceededLimitException extends AuctionException {

    public BidExceededLimitException(BigDecimal maxPrice) {
        super("Bid vượt quá giới hạn: " + maxPrice);
    }
}