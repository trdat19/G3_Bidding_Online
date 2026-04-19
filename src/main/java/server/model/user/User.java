package server.model.user;

import server.model.Entity;
import shared.enums.UserRole;

public abstract class User extends Entity{
    protected String username;
    protected String password;
    protected UserRole role;

    public User(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public abstract String getRole();

    public String getUsername() {
        return username;
    }

    public String getPassword() { return password; }
}
