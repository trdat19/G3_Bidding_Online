package server.model.user;

import shared.enums.UserRole;

public class Admin extends User{

    public Admin(String username, String password) {
        super(username, password);
        role = UserRole.ADMIN;
    }

    @Override
    public String getRole() {
        return role.name();
    }
}
