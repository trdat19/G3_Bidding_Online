package server.exception;

public class AuctionClosedException extends AuctionException{

    public AuctionClosedException(String message) {
        super(message);
    }

    public AuctionClosedException() {
        super("Auction đã đóng");
    }
}
