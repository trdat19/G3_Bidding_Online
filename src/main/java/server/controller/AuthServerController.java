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
            String user = data.get("username");
            String pass = data.get("password");

            // Giả sử AuthService đã có Singleton
            if (AuthService.getInstance().login(user, pass)) {
                // QUAN TRỌNG: Gắn tên người dùng với đường truyền Socket này
                RealtimePushServer.registerUser(user, handler);

                System.out.println(">>> [Auth] User " + user + " đã online.");
                return new BaseResponse(true, "Chào " + user + "!", user);
            }
            return new BaseResponse(false, "Sai tài khoản/mật khẩu", null);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi định dạng đăng nhập", null);
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