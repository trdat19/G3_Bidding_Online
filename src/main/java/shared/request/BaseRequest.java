package shared.request;
import java.io.Serializable;

public class BaseRequest implements Serializable {
    private String action; // Ví dụ: "LOGIN"
    private Object data;   // Chứa User hoặc Bid object

    public BaseRequest(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() { return action; }
    public Object getData() { return data; }

}