package shared.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends AuctionException {

    public InsufficientBalanceException(BigDecimal balance) {
        super("Không đủ tiền. Số dư hiện tại: " + balance);
    }
}