package server.model.user;

<<<<<<< HEAD
=======
import java.sql.Timestamp;
>>>>>>> cc24ab1a490f8327e96db13cc7dbc03ad46ad134
import shared.enums.UserRole;

public class Seller extends User{

<<<<<<< HEAD
    public Seller(String username, String password) {
        super(username, password);
=======
    public Seller(String username, String password, Timestamp createdAt) {
        super(username, password,createdAt);
>>>>>>> cc24ab1a490f8327e96db13cc7dbc03ad46ad134
        role = UserRole.SELLER;
    }

    @Override
    public String getRole() {
        return role.name();
    }
}
