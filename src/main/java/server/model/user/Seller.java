package server.model.user;

import shared.enums.UserRole;

import java.io.Serial;
import java.math.BigDecimal;

public class Seller extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal totalEarnings;

    public Seller(){}

    public Seller(String username, String password, String fullname, String email) {
        super(username, password, fullname, email);
        this.role = UserRole.SELLER;
        totalEarnings = new BigDecimal(0);
    }

    @Override
    public String getInfo() {
        return super.getInfo() + String.format(" | Earnings: %s", totalEarnings);
    }

    //getter
    public BigDecimal getTotalEarnings() { return totalEarnings; }

    //setter
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
}