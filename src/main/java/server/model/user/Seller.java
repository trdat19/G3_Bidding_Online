package server.model.user;

import java.sql.Timestamp;

public class Seller extends User{

    public Seller(String username, String password,Timestamp createdAt) {
        super(username, password,createdAt);
    }

    @Override
    public String getRole() {
        return "SELLER";
    }
}
