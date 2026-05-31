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
    private static volatile AuthService instance;

    private final UserDAO userDAO;

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
            if (user.getStatus() != UserStatus.ACTIVE) {
                return null;
            }
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
            throw new Exception("Tên đăng nhập này đã tồn tại!");

        }
        if (userDAO.existsByEmail(email)) {
            throw new Exception("Email này đã được sử dụng!");
        }
        User newUser = null;
        switch (role) {
            case ADMIN: {
                newUser = new Admin();
                break;
            }
            case SELLER: {
                newUser = new Seller();
                break;
            }
            case BIDDER: {
                newUser = new Bidder();
                break;
            }
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
            throw new Exception("Đăng kí không thành công!");
        }
    }

    public void logout(ClientConnectionHandler handler) {
        User user = handler.getUser();
        if (user != null) {
            System.out.println(">>> [AuthService] User " + user.getUsername() + " đã offline.");
            handler.setUser(null); // Xóa thông tin user khỏi handler
        }
    }

    public void changePassword(User sessionUser, String oldPassword, String newPassword)
            throws Exception {
        if (sessionUser == null || sessionUser.getId() == null) {
            throw new Exception("Phiên đăng nhập không hợp lệ.");
        }

        User storedUser = userDAO.findById(sessionUser.getId());
        if (storedUser == null || !oldPassword.equals(storedUser.getPassword())) {
            throw new Exception("Mật khẩu cũ không chính xác.");
        }
        if (newPassword.equals(storedUser.getPassword())) {
            throw new Exception("Mật khẩu mới phải khác mật khẩu cũ.");
        }
        if (!userDAO.updatePassword(sessionUser.getId(), newPassword)) {
            throw new Exception("Không thể cập nhật mật khẩu.");
        }

        sessionUser.setPassword(newPassword);
    }

    public void verifySession() {
        // Kiểm tra token hoặc session sau này
    }
}
