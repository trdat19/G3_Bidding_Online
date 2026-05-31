package shared.dto.request;
import shared.enums.Action;

import java.io.Serial;
import java.io.Serializable;

public class BaseRequest implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private Action action; // Ví dụ: "LOGIN"
    private Object data;   // Chứa User hoặc Bid object

    public BaseRequest(Action action, Object data) {
        this.action = action;
        this.data = data;
    }

    public Action getAction() { return action; }
    public Object getData() { return data; }

}