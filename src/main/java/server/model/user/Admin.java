package server.model.user;

import shared.enums.UserRole;
import java.sql.Timestamp;

public class Admin extends User{

    public Admin(String username, String password, Timestamp createAt) {
        super(username, password, createAt);
        role = UserRole.ADMIN;
    }

    @Override
    public String getRole() {
        return role.name();
    }
}
