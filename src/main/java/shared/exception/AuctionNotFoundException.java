package shared.exception;

public class AuctionNotFoundException extends AuctionException {

    public AuctionNotFoundException(Long auctionId) {
        super("Auction không tồn tại: " + auctionId);
    }
}