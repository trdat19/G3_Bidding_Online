package shared.dto.response.bid;
import shared.dto.response.BaseResponse;

public class NewBidUpdateResponse extends BaseResponse {
    private final double currentPrice;

    public NewBidUpdateResponse(double currentPrice) {
        super(true, "NEW_BID_UPDATE");
        this.currentPrice = currentPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }
}