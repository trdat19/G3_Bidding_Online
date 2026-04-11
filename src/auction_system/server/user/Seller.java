package auction_system.model.user;

public class Seller extends User{

    public Seller(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "SELLER";
    }
}
