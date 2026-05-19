package server.service;

import server.dao.UserDAO;
import server.model.user.Admin;
import server.model.user.Bidder;
import server.model.user.Seller;
import server.model.user.User;
import server.network.ClientConnectionHandler;
import shared.enums.UserRole;
import shared.enums.UserStatus;

public class AuthService {

    // 1. Singleton: Đảm bảo duy nhất 1 thực thể quản lý User online
    private static AuthService instance;

    private UserDAO userDAO;

    // Khóa constructor để không ai 'new' lung tung
    private AuthService() {
        userDAO = new UserDAO();
    }

    public static AuthService getInstance() {
        if (instance == null) {

            synchronized (AuthService.class) {
                instance = new AuthService();
            }
        }
        return instance;
    }

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            System.out.println(">>> [AuthService] Đăng nhập thành công: " + username);
            return user;
        }
        System.out.println(">>> [AuthService] Đăng nhập thất bại: " + username);
        return null;

    }

    public User register(String username, String password, String fullName,
                         String email, UserRole role ) throws Exception
    {
        if (userDAO.existsByUsername(username)) {
            throw new Exception("USERNAME_EXISTS");

        }
        if (userDAO.existsByEmail(email)) {
            throw new Exception("EMAIL_EXISTS");
        }
        User newUser = null;
        switch (role) {
            case ADMIN:
                newUser = new Admin();
            break;
            case SELLER:
                newUser = new Seller();
                break;
            case BIDDER:
                newUser = new Bidder();
                break;
        }

        newUser.setUsername(username);
        newUser.setPassword(password); // Nên mã hóa mật khẩu nếu có thời gian
        newUser.setFullname(fullName);
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setStatus(UserStatus.ACTIVE); // Mặc định tài khoản mới là Active
        newUser.setCreatedAt(java.time.LocalDateTime.now());

        boolean isInserted = userDAO.insertUser(newUser);

        // 6. Trả về kết quả
        if (isInserted) {
            System.out.println(">>> [AuthService] Đăng ký thành công: " + username);
            return newUser;
        } else {
            throw new Exception("Có lỗi xảy ra khi đăng kí!");
        }
    }

    public void logout(ClientConnectionHandler handler) {
        User user = handler.getUser();
        if (user != null) {
            System.out.println(">>> [AuthService] User " + user.getUsername() + " đã offline.");
            handler.setUser(null); // Xóa thông tin user khỏi handler
        }
    }

    public void verifySession() {
        // Kiểm tra token hoặc session sau này
    }
}