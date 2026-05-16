package shared.dto.request;

import shared.enums.Action;

import java.io.Serializable;

/**
 * Lớp cha chung của mọi request
 * Có action và mỗi lớp con sẽ tự giữ field cần thiết
 */
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Action action; // Ví dụ: "LOGIN"

    protected BaseRequest(Action action) {
        this.action = action;
    }

    public Action getAction() { return action; }
}