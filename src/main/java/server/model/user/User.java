package server.model.user;

<<<<<<< HEAD
=======
import java.sql.Timestamp;
>>>>>>> cc24ab1a490f8327e96db13cc7dbc03ad46ad134
import server.model.Entity;
import shared.enums.UserRole;

public abstract class User extends Entity{
    protected String username;
    protected String password;
    protected UserRole role;
<<<<<<< HEAD
=======
    protected Timestamp createdAt;
>>>>>>> cc24ab1a490f8327e96db13cc7dbc03ad46ad134

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
