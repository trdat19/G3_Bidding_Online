package server.controller;

import server.network.ClientConnectionHandler;
import server.network.RealtimePushServer;
import server.service.AuthService;
import shared.request.BaseRequest;
import shared.response.BaseResponse;
import java.util.Map;

public class AuthServerController {
    // 1. Singleton Instance
    private static AuthServerController instance;
    private AuthServerController() {}

    public static synchronized AuthServerController getInstance() {
        if (instance == null) instance = new AuthServerController();
        return instance;
    }

    // 2. Xử lý Đăng nhập
    public BaseResponse login(BaseRequest request, ClientConnectionHandler handler) {
        try {
            Map<String, String> data = (Map<String, String>) request.getData();
            String username = data.get("username");
            String pass = data.get("password");

            // 1. Gọi AuthService (Hàm này bạn đã sửa để trả về User thay vì boolean)
            server.model.user.User loggedInUser = AuthService.getInstance().login(username, pass);

            if (loggedInUser != null) {
                // 2. Đăng ký Realtime bằng username
                RealtimePushServer.registerUser(username, handler);

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

    // 3. Xử lý Đăng ký
    public BaseResponse register(BaseRequest request) {
        // Logic gọi AuthService.register(...)
        return new BaseResponse(true, "Đăng ký thành công!", null);
    }

    // 4. Xử lý Đăng xuất
    public BaseResponse logout(BaseRequest request, ClientConnectionHandler handler) {
        // Hủy đăng ký trên Realtime Server khi thoát
        RealtimePushServer.removeConnection(handler);
        return new BaseResponse(true, "Đã đăng xuất", null);
    }
}