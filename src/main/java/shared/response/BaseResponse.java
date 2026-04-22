package shared.response;

import java.io.Serializable;

public class BaseResponse implements Serializable {
    private boolean success;
    private String action;
    private Object data;
    private String message; // Thêm biến này để hiển thị thông báo ra màn hình

    // Constructor dùng ể thông bào login than công/ thất bại
    public BaseResponse(boolean success, String action, Object data) {
        this.success = success;
        this.action = action;
        this.data = data;
    }

    // Constructor dùng cho Realtime (có thêm Action để Client biết tin nhắn loại gì)
    public BaseResponse(boolean success, String action, String message, Object data) {
        this.success = success;
        this.action = action;
        this.message = message;
        this.data = data;
    }
    public String getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }


}