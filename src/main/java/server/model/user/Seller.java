package server.model.user;

import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.sql.Timestamp;

public class Seller extends User {
    public Seller(){}

    public Seller(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, UserRole.SELLER, UserStatus.ACTIVE);
    }

    public Seller(Long id, String username, String password, String fullName, String email,
                  UserStatus status, Timestamp createdAt) {
        super(id, username, password, fullName, email, UserRole.SELLER, status, createdAt);
    }
}