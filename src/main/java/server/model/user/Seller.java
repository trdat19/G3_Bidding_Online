package server.model.user;

import shared.enums.UserRole;

public class Seller extends User{

    public Seller(String username, String password) {
        super(username, password);
        role = UserRole.SELLER;
    }

    @Override
    public String getRole() {
        return role.name();
    }
}
