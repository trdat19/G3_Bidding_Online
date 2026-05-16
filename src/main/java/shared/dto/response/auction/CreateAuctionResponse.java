package shared.dto.response.auction;

import shared.dto.common.AuctionDTO;
import shared.dto.response.BaseResponse;

public class CreateAuctionResponse extends BaseResponse {
    private final AuctionDTO auction;

    public CreateAuctionResponse(AuctionDTO auction) {
        super(true, "Tạo phiên đấu giá thành công");
        this.auction = auction;
    }

    public AuctionDTO getAuction() {
        return auction;
    }
}