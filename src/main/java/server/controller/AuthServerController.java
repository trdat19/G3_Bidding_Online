package server.controller;

import server.model.user.User;
import server.network.ClientConnectionHandler;
import server.network.RealtimePushServer;
import server.service.AuthService;
import shared.dto.request.LoginRequest;
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

    public static synchronized AuthServerController getInstance() {
        if (instance == null) instance = new AuthServerController();
        return instance;
    }

    public BaseResponse login(LoginRequest request, ClientConnectionHandler handler) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();

            User loggedInUser = AuthService.getInstance().login(username, password);

            if (loggedInUser != null) {
                // 1. Lưu thông tin user vào handler để quản lý Session
                handler.setUsetr(loggedInUser);

                // 2. Đăng ký Realtime bằng username
                RealtimePushServer.registerUser(loggedInUser.getId(), handler);

                System.out.println(">>> [Auth] User " + username + " đã online.");

                // 3. QUAN TRỌNG: Gửi ĐỐI TƯỢNG loggedInUser về (KHÔNG gửi username)
                return new BaseResponse(true, "Chào " + loggedInUser.getFullName() + "!", loggedInUser);
            }

            return new BaseResponse(false, "Sai tài khoản/mật khẩu", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse(false, "Lỗi hệ thống: " + e.getMessage(), null);
        }
    }

    // Xử lý Đăng ký
    public BaseResponse register(BaseRequest request) {
        try {
            // 1. Ép kiểu lấy Map dữ liệu từ request
            Map<String, String> data = (Map<String, String>) request.getData();

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
            return new BaseResponse(false, e.getMessage(), null);
        }
    }

    // 4. Xử lý Đăng xuất
    public BaseResponse logout(BaseRequest request, ClientConnectionHandler handler) {
        // Hủy đăng ký trên Realtime Server khi thoát
        RealtimePushServer.removeConnection(handler);
        return new BaseResponse(true, "Đã đăng xuất", null);
    }
}