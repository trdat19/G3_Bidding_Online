package shared.request;
import java.io.Serializable;


public class BaseRequest implements Serializable {
    private shared.enums.Action action; // Ví dụ: "LOGIN"
    private Object data;   // Chứa User hoặc Bid object

    public BaseRequest( shared.enums.Action action , Object data) {
        this.action = action;
        this.data = data;
    }

    public shared.enums.Action getAction() { return action; }
    public Object getData() { return data; }

}