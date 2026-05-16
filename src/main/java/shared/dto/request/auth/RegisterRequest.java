package shared.dto.request.auth;

import shared.dto.request.BaseRequest;
import shared.enums.Action;
import shared.enums.UserRole;

public class RegisterRequest extends BaseRequest {
    private String username;
    private String fullname;
    private String email;
    private String password;
    private UserRole role;

    public RegisterRequest(String username, String fullname, String email,
                           String password, UserRole role) {
        super(Action.REGISTER);
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
    }

    //GETTER
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullname() { return fullname; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }

}
