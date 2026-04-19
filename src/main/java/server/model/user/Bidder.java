package server.model.user;

import shared.enums.UserRole;

public class Bidder extends User{

    public Bidder(String username, String password) {
        super(username, password);
        role = UserRole.BIDDER;
    }

    @Override
    public String getRole() {
        return role.name();
    }
}
