package shared.exception;

import java.time.LocalDateTime;

public class InvalidAuctionTimeException extends AuctionException {

    public InvalidAuctionTimeException(LocalDateTime now) {
        super("Không thể bid tại thời điểm: " + now);
    }
}