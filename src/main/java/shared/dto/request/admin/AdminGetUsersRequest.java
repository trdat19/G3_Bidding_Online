package shared.dto.request.admin;

import shared.dto.request.BaseRequest;
import shared.enums.Action;

public class AdminGetUsersRequest extends BaseRequest {
    public AdminGetUsersRequest() {
        super(Action.ADMIN_GET_USERS);
    }
}