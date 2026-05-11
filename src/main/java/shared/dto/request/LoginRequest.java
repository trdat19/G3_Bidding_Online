package shared.dto.request;

import java.io.Serializable;

public class LoginRequest implements Serializable {
    private String username;
    private String password;

    //GETTER
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    //SETTER

}
