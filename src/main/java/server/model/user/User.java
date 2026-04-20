package server.model.user;

import java.sql.Timestamp;
import server.model.Entity;
import shared.enums.UserRole;

public abstract class User extends Entity{
    protected String username;
    protected String password;
    protected String fullname;
    protected String email;
    protected UserRole role;
    protected Timestamp createdAt;

    public User(String username, String password , Timestamp crTimestamp) {
        super();
        this.username = username;
        this.password = password;
        this.createdAt = crTimestamp;
    }

    public abstract String getRole();

    public String getUsername() {
        return username;
    }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getFullname() { return fullname; }
}
