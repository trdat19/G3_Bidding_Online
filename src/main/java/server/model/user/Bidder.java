package server.model.user;

import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.sql.Timestamp;

public class Bidder extends User {
    public Bidder(){}

    public Bidder(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, UserRole.BIDDER, UserStatus.ACTIVE);
    }

    public Bidder(Long id, String username, String password, String fullName, String email,
                  UserStatus status, Timestamp createdAt) {
        super(id, username, password, fullName, email, UserRole.BIDDER, status, createdAt);
    }
}