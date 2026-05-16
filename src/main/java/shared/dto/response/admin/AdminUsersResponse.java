package shared.dto.response.admin;

import shared.dto.common.UserDTO;
import shared.dto.response.BaseResponse;

import java.util.List;

public class AdminUsersResponse extends BaseResponse {
    private final List<UserDTO> users;

    public AdminUsersResponse(List<UserDTO> users) {
        super(true, "Load users successfully");
        this.users = users;
    }

    public List<UserDTO> getUsers() {
        return users;
    }
}