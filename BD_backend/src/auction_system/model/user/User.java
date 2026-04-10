package auction_system.model.user;

import auction_system.model.Entity;

public abstract class User extends Entity{
    protected String username;
    protected String password;

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
