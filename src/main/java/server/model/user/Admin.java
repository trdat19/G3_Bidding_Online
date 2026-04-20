package server.model.user;

import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.sql.Timestamp;

public class Admin extends User {
    public Admin(){}

    public Admin(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, UserRole.ADMIN, UserStatus.ACTIVE);
    }

    public Admin(Long id, String username, String password, String fullName, String email,
                 UserStatus status, Timestamp createdAt) {
        super(id, username, password, fullName, email, UserRole.ADMIN, status, createdAt);
    }
}