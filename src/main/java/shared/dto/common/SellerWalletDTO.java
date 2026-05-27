package shared.dto.common;

import java.io.Serializable;
import java.math.BigDecimal;

public class SellerWalletDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BigDecimal availableBalance;
    private final BigDecimal totalRevenue;
    private final long soldProductCount;

    public SellerWalletDTO(BigDecimal availableBalance, BigDecimal totalRevenue,
                           long soldProductCount) {
        this.availableBalance = availableBalance;
        this.totalRevenue = totalRevenue;
        this.soldProductCount = soldProductCount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getSoldProductCount() {
        return soldProductCount;
    }
}
