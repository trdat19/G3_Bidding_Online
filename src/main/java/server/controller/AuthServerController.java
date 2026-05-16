package server.controller;

import server.model.user.User;
import server.network.ClientConnectionHandler;
import server.network.RealtimePushServer;
import server.service.AuthService;
import shared.dto.common.UserDTO;
import shared.dto.request.auth.LoginRequest;
import shared.dto.request.auth.RegisterRequest;
import shared.dto.response.ErrorResponse;
import shared.dto.response.auth.LoginResponse;
import shared.dto.response.auth.RegisterResponse;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

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

    public static AuthServerController getInstance() {
        if (instance == null) {
            instance = new AuthServerController();
        }
        return instance;
    }

    public BaseResponse login(LoginRequest request, ClientConnectionHandler handler) {
        try {

            String username = request.getUsername();
            String password = request.getPassword();

            User loggedInUser = AuthService.getInstance().login(username, password);

            if (loggedInUser != null) {
                // 1. Lưu thông tin user vào handler để quản lý Session
                handler.setUser(loggedInUser);

                // 2. Đăng ký Realtime bằng username
                RealtimePushServer.registerUser(loggedInUser.getId(), handler);

                System.out.println(">>> [Auth] User " + username + " đã online.");

                // 3. QUAN TRỌNG: Gửi ĐỐI TƯỢNG loggedInUser về (KHÔNG gửi username)
                UserDTO userDTO = new UserDTO(
                        loggedInUser.getId(),
                        loggedInUser.getUsername(),
                        loggedInUser.getFullName(),
                        loggedInUser.getEmail(),
                        loggedInUser.getRole(),
                        loggedInUser.getStatus(),
                        loggedInUser.getCreatedAt()
                );

                return new LoginResponse(userDTO);
            }

            return new BaseResponse(false, "Sai tài khoản/mật khẩu");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse(false, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Xử lý Đăng ký
    public BaseResponse register(RegisterRequest request) {
        try {
            User newUser = AuthService.getInstance().register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFullname(),
                    request.getEmail(),
                    request.getRole()
            );
            return new BaseResponse(true, "Đăng ký thành công!");

        }
        catch (Exception e)
        {
            return new ErrorResponse(e.getMessage());
        }
    }

    // 4. Xử lý Đăng xuất
    public BaseResponse logout(BaseRequest request, ClientConnectionHandler handler) {
        // Hủy đăng ký trên Realtime Server khi thoát
        RealtimePushServer.removeConnection(handler);
        return new BaseResponse(true, "Đã đăng xuất");
    }
}