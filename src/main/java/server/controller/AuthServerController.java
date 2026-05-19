package server.controller;

import server.model.user.User;
import server.network.ClientConnectionHandler;
import server.network.RealtimePushServer;
import server.service.AuthService;

import shared.enums.UserRole;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.util.Map;

/**
 * Quản lý xác thực người dùng khi gửi yêu cầu đăng kí
 * đăng nhập, đăng xuất vào server
 *
 * Singleton đảm bảo không bị lost dữ liệu - nhiều máy đăng kí username password giống nhau
 *                                            đăng nhập cùng lúc ở nhiều nơi, ....
 */

public class AuthServerController {
    private static AuthServerController instance;

    private AuthServerController() {}

    //double-checked locking
    public static AuthServerController getInstance() {
        if (instance == null) {

            synchronized (AuthServerController.class) {
                if (instance == null) {
                    instance = new AuthServerController();
                }
            }
        }
        return instance;
    }

    //1. Đăng nhập
    public BaseResponse login(BaseRequest request, ClientConnectionHandler handler) {
        try {
            Map<String, String> data = (Map<String, String>) request.getData();

            if (!(data.containsKey("username") && data.containsKey("password"))) {
                return new BaseResponse(false, "Thiếu username hoặc password", null);
            }

            String username = data.get("username");
            String password = data.get("password");

            User loggedInUser = AuthService.getInstance().login(username, password);

            if (loggedInUser != null) {
                // 1. Lưu thông tin user vào handler để quản lý Session
                handler.setUser(loggedInUser);

                // 2. Đăng ký Realtime bằng userId
                RealtimePushServer.registerUser(loggedInUser.getId(), handler);

                System.out.println(">>> [Auth] User " + username + " đã online.");

                // 3. QUAN TRỌNG: Gửi ĐỐI TƯỢNG loggedInUser về (KHÔNG gửi username)
                return new BaseResponse(true,
                        String.format("Chào %s!", loggedInUser.getFullName()),
                        loggedInUser);
            }

            return new BaseResponse(false, "Sai tài khoản/mật khẩu", null);
        } catch (Exception e) {
            //e.printStackTrace();
            return new BaseResponse(false,
                    String.format("Lỗi hệ thống: %s", e.getMessage()),
                    null);
        }
    }

    //2. Đăng ký
    public BaseResponse register(BaseRequest request) {
        try {
            // 1. Ép kiểu lấy Map dữ liệu từ request
            Map<String, String> data = (Map<String, String>) request.getData();

            if (!data.containsKey("username")
                    || !data.containsKey("password")
                    || !data.containsKey("fullname")
                    || !data.containsKey("email")
                    || !data.containsKey("role"))
            {
                return new BaseResponse(false,
                        "Thiếu thông tin đăng ký cần thiết!", null);
            }

            // 2. Gọi AuthService để xử lý logic (hàm này có throws Exception)
            User newUser = AuthService.getInstance().register(
                    data.get("username"),
                    data.get("password"),
                    data.get("fullname"),
                    data.get("email"),
                    UserRole.valueOf(data.get("role"))
            );

            // 3. Nếu không có Exception nào bị ném ra, nghĩa là thành công
            return new BaseResponse(true, "Đăng ký thành công!", newUser);

        }
        catch (Exception e)
        {
            return new BaseResponse(false,
                    String.format("Đăng kí thất bại: %s", e.getMessage()),
                    null);
        }
    }

    // 3.Đăng xuất
    public BaseResponse logout(ClientConnectionHandler handler) {
        AuthService.getInstance().logout(handler);
        RealtimePushServer.removeConnection(handler);
        return new BaseResponse(true, "Đã đăng xuất", null);
    }
}