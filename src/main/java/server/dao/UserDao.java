/*package server.dao;

import server.config.DBconnection;
import server.model.user.Admin;
import server.model.user.Bidder;
import server.model.user.Seller;
import server.model.user.User;
import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    // thêm user mới vào table
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users(username, password, full_name, email, role, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus().name());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys(); // lấy ra khoá tự sinh mà DB vừa tạo
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("insertUser error: " + e.getMessage());
        }
        return false;
    }

    // tìm user theo id
    public User findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // tìm user theo username
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // lấy tất cả users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // cập nhật lại từ đầu users
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, " +
                     "email = ?, role = ?, status = ? WHERE id = ?";

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus().name());
            ps.setLong(7, user.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // đổi mật khẩu user
    public boolean UpdatePassword(long id , String passwordNew) {
        String sql = "UPDATE users SET password = ? Where id = ?";
        try(Connection con = DBconnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1,passwordNew);
            ps.setLong(2,id);
            int row = ps.executeUpdate();
            return row > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false ;
    }
    // đổi username
    public boolean updateUsername(long id, String newUsername) {
        String sql = "UPDATE users SET username = ? WHERE id = ?";
        try (Connection con = DBconnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setLong(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateUsername error: " + e.getMessage());
            return false;
        }
    }
    // đổi trạng thái
    public boolean updateStatus(long id, UserStatus status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Update status error: " + e.getMessage());
        }
        return false;
    }
    // xoá user
    public boolean deleteUser(long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // kiểm tra xem username tồn tại chưa
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection con = DBconnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("existsByUsername error: " + e.getMessage());
        }
        return false;
    }

    // kiểm tra email đã tồn tại hay chưa
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("existsByEmail error: " + e.getMessage());
        }
        return false;
    }

    // ánh xạ map chuyển đổi dữ liệu DB thành object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;
        switch (role) {
            case "ADMIN":
                user = new Admin();
                break;
            case "SELLER":
                user = new Seller();
                break;
            default:
                user = new Bidder();
                break;
        }
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullname(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
    // Test
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (
                Connection con = DBconnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("findAll error: " + e.getMessage());
        }
        return users;
    }
}*/
