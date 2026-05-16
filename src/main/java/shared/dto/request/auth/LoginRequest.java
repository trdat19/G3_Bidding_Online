package shared.dto.request.auth;

import shared.dto.request.BaseRequest;
import shared.enums.Action;

public class LoginRequest extends BaseRequest {

    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        super(Action.LOGIN);
        this.username = username;
        this.password = password;
    }

    //GETTER
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
