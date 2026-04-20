package server.model.user;

import java.sql.Timestamp;
import shared.enums.UserRole;

public class Seller extends User{

    public Seller(String username, String password, Timestamp createdAt) {
        super(username, password,createdAt);
        role = UserRole.SELLER;
    }

    @Override
    public String getRole() {
        return role.name();
    }
}
