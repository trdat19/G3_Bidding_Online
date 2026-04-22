package shared.response;

import java.io.Serializable;

public class BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L; // Đảm bảo đồng bộ khi truyền qua Socket

    private boolean success;
    private String action;
    private Object data;
    private String message;

    // Constructor 1: Đầy đủ tham số
    public BaseResponse(boolean success, String action, String message, Object data) {
        this.success = success;
        this.action = action;
        this.message = message;
        this.data = data;
    }

    // Constructor 2: Rút gọn (tự tạo message mặc định)
    public BaseResponse(boolean success, String action, Object data) {
        this.success = success;
        this.action = action;
        this.data = data;
        this.message = success ? "Thành công" : "Thất bại";
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