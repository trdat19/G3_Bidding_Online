package server.service;

import server.dao.UserDAO;
import server.model.user.User;

public class AuthService {
    // 1. Singleton: Đảm bảo duy nhất 1 thực thể quản lý User online
    private static AuthService instance;

    // Khóa constructor để không ai 'new' lung tung
    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
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
        UserDAO userDao = new UserDAO();
        User user = userDao.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            System.out.println(">>> [AuthService] Đăng nhập thành công: " + username);
            return user;
        }
        System.out.println(">>> [AuthService] Đăng nhập thất bại: " + username);
        return null;
    }

    public void register() {
        // Sau này code DB vào đây
    }

    public void logout() {
        // Xử lý logic đăng xuất nếu cần
    }

    public void verifySession() {
        // Kiểm tra token hoặc session sau này
    }
}