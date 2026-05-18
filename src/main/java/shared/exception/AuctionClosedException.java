package shared.exception;

public class AuctionClosedException extends AuctionException{

    public AuctionClosedException() {
        super("Auction đã đóng");
    }
}
