package server.service;

import server.dao.UserDAO;
import server.model.user.Admin;
import server.model.user.Bidder;
import server.model.user.Seller;
import server.model.user.User;
import shared.enums.UserRole;
import shared.enums.UserStatus;

public class AuthService {
    private UserDAO userDAO;

    // Constructor cho test
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // 1. Singleton: Đảm bảo duy nhất 1 thực thể quản lý User online
    private static AuthService instance;

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

    // 2. Logic kiểm tra đăng nhập
//    public boolean login(String username, String password) {
//        // Tạm thời cho phép 2 tài khoản này để test Realtime
//        if ((username.equals("user1") && password.equals("123")) ||
//                (username.equals("user2") && password.equals("123"))) {
//
//            System.out.println(">>> [AuthService] Xác thực thành công: " + username);
//            return true;
//        }
//        return false;
//    }
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
            throw new Exception("DATABASE_ERROR");
        }
    }

    public void logout() {
        // Xử lý logic đăng xuất nếu cần
    }

    public void verifySession() {
        // Kiểm tra token hoặc session sau này
    }
}