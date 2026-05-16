package shared.dto.request.bid;

import shared.dto.request.BaseRequest;
import shared.enums.Action;

public class PlaceBidRequest extends BaseRequest {
    private final long auctionId;
    private final double amount;

    public PlaceBidRequest(long auctionId, double amount) {
        super(Action.PLACE_BID);
        this.auctionId = auctionId;
        this.amount = amount;
    }

    public long getAuctionId() { return auctionId; }
    public double getAmount() { return amount; }
}