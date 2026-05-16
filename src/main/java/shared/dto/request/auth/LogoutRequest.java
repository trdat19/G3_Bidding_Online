package shared.dto.request.auth;

import shared.dto.request.BaseRequest;
import shared.enums.Action;

public class LogoutRequest extends BaseRequest {

    public LogoutRequest() {
        super(Action.LOGOUT);
    }
}
