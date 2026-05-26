package server.exception;

import java.time.LocalDateTime;

public class InvalidBidException extends AuctionException{

    public InvalidBidException(String message) {
        super(message);
    }

    public InvalidBidException() {
        super("Bid đặt không hợp lệ!");
    }

    public InvalidBidException(double amount) {
        super("Mức đặt " + amount + " thấp hơn mức tối thiểu!");
    }

    public InvalidBidException(LocalDateTime timestamp) {
        super("Thời gian đặt Bid: " + timestamp + " ngoài thời gian cho phép!");
    }
}
