package server.service;

import server.dao.UserDAO;
import server.model.user.User;
import shared.enums.UserStatus;

import java.util.List;

/**
 * UserService – xử lý logic nghiệp vụ liên quan đến người dùng, admin sẽ gọi
 *
 * Các chức năng:
 *   - Tìm kiếm người dùng
 *   - Thay đổi trạng thái (kích hoạt, khóa)
 *   Phân quyền (admin, user)
 *
 * Singleton
 */
public class UserService {
    private static UserService instance = null;

    private final UserDAO userDAO =  new UserDAO();

    private UserService() {}

    //double-checked locking
    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    // ---------------------- QUERY ----------------------

    public List<User> findAllUsers() {
        return userDAO.getAllUsers();
    }

    public User findUser(Long userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng #" + userId);
        }
        return user;
    }

    //thống kê
    public long countAllUsers() {
        return userDAO.getAllUsers().size();
    }

    // ---------------------- STATUS ----------------------


    public boolean changeStatus(Long userId, UserStatus status) {
        // Kiểm tra tồn tại trước khi update
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng #" + userId);
        }

        return userDAO.updateStatus(userId, status);
    }

    // ---------------------- ROLE ----------------------

//    //phân quyền
//    public boolean assignRole(Long userId, String role) {
//        User user = userDAO.findById(userId);
//        if (user == null) {
//            throw new IllegalArgumentException("Không tìm thấy người dùng #" + userId);
//        }
//
//        return userDAO.updateRole(userId, role);
//    }
}
