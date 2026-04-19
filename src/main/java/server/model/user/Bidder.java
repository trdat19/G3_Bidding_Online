package server.model.user;

import java.sql.Timestamp;

import shared.enums.UserRole;

public class Bidder extends User{

    public Bidder(String username, String password , Timestamp createdAt) {
        super(username, password , createdAt);
        role = UserRole.BIDDER;
    }

    @Override
    public String getRole() {
        return role.name();
    }
}
