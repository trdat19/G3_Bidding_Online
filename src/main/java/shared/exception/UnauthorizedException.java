package shared.exception;

//custom exception liên quan phần kiểm tra xác thực
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
