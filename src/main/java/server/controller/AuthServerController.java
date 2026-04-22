package server.controller;

import server.network.ClientConnectionHandler;
import server.network.RealtimePushServer;
import server.service.AuthService;
import shared.request.BaseRequest;
import shared.response.BaseResponse;

import java.util.HashMap;
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
                RealtimePushServer.registerUser(user, handler);

                Map<String, Object> userData = new HashMap<>();
                userData.put("username", user);
                userData.put("role", "SELLER"); // test

                return new BaseResponse(true, "LOGIN", "Chào " + user + "!", userData);
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