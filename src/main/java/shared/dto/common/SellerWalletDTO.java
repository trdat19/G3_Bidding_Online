package shared.dto.common;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public class SellerWalletDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal availableBalance;
    private BigDecimal totalRevenue;
    private long soldProductCount;

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
