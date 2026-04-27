package shared.response;

import java.io.Serializable;
// gói phản hồi chung gửi từ server về client
public class BaseResponse implements Serializable {
    private boolean success;
    private String action;
    private Object data;

    public BaseResponse(boolean success, String action, Object data) {
        this.success = success;
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }
}