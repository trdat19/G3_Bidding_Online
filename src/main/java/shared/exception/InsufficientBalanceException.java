package shared.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends AuctionException {

    public InsufficientBalanceException(BigDecimal availableBalance) {
        super("Không đủ tiền khả dụng để đặt giá. Số dư khả dụng: $" + availableBalance);
    }
}