package shared.dto.response;

//import shared.enums.Action;

import java.io.Serializable;

/**
 * gói phản hồi chung gửi từ server về client
  */

public class BaseResponse implements Serializable {

    private static final long serialVersionUID = 1L; // Đảm bảo đồng bộ khi truyền qua Socket

    private boolean success;
    private String message;
    // private Action action;

    public BaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // --- CÁC PHƯƠNG THỨC GETTER (Để lấy dữ liệu) ---

    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    // public Action getAction() { return action; }

    // --- CÁC PHƯƠNG THỨC SETTER (Để gán dữ liệu nếu cần) ---

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    // public void setAction(Action action) { this.action = action;}

    /**
     * ErrorResponse tạo response lỗi nhanh, khi không cần subclass riêng
     * VD: validateion fail, không thấy resource, ...
     */
    public static ErrorResponse error(String message) {
        return new ErrorResponse(message);
    }
}