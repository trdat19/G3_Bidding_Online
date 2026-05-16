package shared.dto.response;

public class ErrorResponse extends BaseResponse{

    private static final long serialVersionUID = 1L;

    public ErrorResponse(String message) {
        super(false, message);
    }
}
