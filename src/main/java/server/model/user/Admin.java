package server.model.user;

import shared.enums.UserRole;

import java.io.Serial;

public class Admin extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    public Admin() {}

    public Admin(String username, String password, String fullname, String email) {
        super(username, password, fullname, email);
        this.role = UserRole.ADMIN;
    }

    @Override
    public String getInfo() {
        return super.getInfo() + " | [ADMIN]";
    }
}