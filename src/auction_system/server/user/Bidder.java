package auction_system.model.user;

public class Bidder extends User{

    public Bidder(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "BIDDER";
    }
}
