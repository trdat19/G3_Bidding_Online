package shared.dto.response.auth;

import shared.dto.common.UserDTO;
import shared.dto.response.BaseResponse;

public class RegisterResponse extends BaseResponse{

    private static final long serialVersionUID = 1L;

    private final UserDTO user;

    public RegisterResponse(UserDTO user) {
        super(true, "Đăng kí thành công!");
        this.user = user;
    }

    public UserDTO getUser() { return user; }
}
