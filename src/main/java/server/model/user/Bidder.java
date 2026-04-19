package server.model.user;

import java.sql.Timestamp;

public class Bidder extends User{

    public Bidder(String username, String password , Timestamp createdAt) {
        super(username, password , createdAt);
    }

    @Override
    public String getRole() {
        return "BIDDER";
    }
}
