package shared.dto.response;

import java.io.Serial;
import java.io.Serializable;
// gói phản hồi chung gửi từ server về client
public class BaseResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // Đảm bảo đồng bộ khi truyền qua Socket

    private boolean success;
    private String action;
    private Object data;
    private String message;

    public BaseResponse(boolean success, String message, Object data) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // --- CÁC PHƯƠNG THỨC GETTER (Để lấy dữ liệu) ---

    public boolean isSuccess() {
        return success;
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

    // --- CÁC PHƯƠNG THỨC SETTER (Để gán dữ liệu nếu cần) ---

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}