package shared.dto.response.auth;

import shared.dto.common.UserDTO;
import shared.dto.response.BaseResponse;

public class LoginResponse extends BaseResponse {

    private static final long serialVersionUID = 1L;

    private final UserDTO user;

    public LoginResponse(UserDTO user) {
        super(true, String.format("Chào mừng trở lại %s!",user.getFullname()));
        this.user = user;
    }

    public UserDTO getUser() { return user; }
}
