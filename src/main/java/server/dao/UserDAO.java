package server.dao;

import server.database.DBConnection;
import server.model.user.Admin;
import server.model.user.Bidder;
import server.model.user.Seller;
import server.model.user.User;
import shared.enums.UserRole;
import shared.enums.UserStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class UserDAO {

    public boolean insertUser(User user) {
        String sql = """
                     INSERT INTO users(username, password, full_name, email, role, status, balance)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                     """;

        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus().name());
            ps.setBigDecimal(7, BigDecimal.ZERO);

            int rowsAffected = ps.executeUpdate(); // 1 nếu insert thành công, 0 nếu thất bại
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys(); // lấy ra khoá tự sinh mà DB vừa tạo
                if (rs.next()) {
                    user.setId(rs.getLong(1)); //lấy id ở cột ID trong DB gán lại cho user;
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
        }
        return false;
    }

    // tìm user theo id
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // dùng khi viết logging
            System.err.println("Lỗi khi tìm người dùng theo ID: " + e.getMessage());
        }
        return null;
    }

    // tìm user theo username
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tìm người dùng theo Username: " + e.getMessage());
        }
        return null;
    }

    // lấy tất cả users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi lấy tất cả người dùng: " + e.getMessage());
        }

        return users;
    }

    //----------------UPDATE--------------------
    // cập nhật lại từ đầu users
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, " +
                     "email = ?, role = ?, status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getStatus().name());
            ps.setLong(7, user.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // đổi mật khẩu user
    public boolean updatePassword(Long id , String passwordNew) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, passwordNew);
            ps.setLong(2, id);
            int row = ps.executeUpdate();
            return row > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi khi cập nhật mật khẩu: " + e.getMessage());
        }
        return false ;
    }

    // đổi username
    public boolean updateUsername(Long id, String newUsername) {
        String sql = "UPDATE users SET username = ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {

            ps.setString(1, newUsername);
            ps.setLong(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tên người dùng: " + e.getMessage());
        }
        return false;
    }

    // đổi trạng thái
    public boolean updateStatus(Long id, UserStatus status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Update status error: " + e.getMessage());
        }
        return false;
    }

    //---------------DELETE-------------------
    // xoá user
    public boolean deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi xoá người dùng: " + e.getMessage());
        }
        return false;
    }

    //------------CHECK-EXISTS-----------
    // kiểm tra xem username tồn tại chưa
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra tồn tại bằng tên người dùng: " + e.getMessage());
        }
        return false;
    }

    // kiểm tra email đã tồn tại hay chưa
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra tồn tại bằng email người dùng: " + e.getMessage());
        }
        return false;
    }

    // ánh xạ map chuyển đổi dữ liệu DB thành object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");

        User user;
        switch (role) {
            case "ADMIN": {
                user = new Admin();
                break;
            }
            case "SELLER": {
                user = new Seller();
                //((Seller) user).setTotalEarning();
                break;
            }
            default: {
                user = new Bidder();
                //((Bidder) user).setBalance();
                //((Bidder) user).setMaxBid();
                //((Bidder) user).setBidIncrement();
                break;
            }
        }

        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullname(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));
        if (user instanceof Bidder bidder) {
            bidder.setBalance(rs.getBigDecimal("balance"));
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        return user;
    }

    //----------Balance----------------
    public BigDecimal getBalance(Long userId) {
        String sql = "SELECT balance FROM users WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy số dư người dùng: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    public boolean increaseBalance(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, amount);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi nạp vào ví: " + e.getMessage());
        }

        return false;
    }

    public boolean decreaseBalanceIfEnough(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String sql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, amount);
            ps.setLong(2, userId);
            ps.setBigDecimal(3, amount);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi rút khỏi ví: " + e.getMessage());
        }

        return false;
    }

    public boolean transferBalanceIfEnough(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String subtractSql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String addSql = "UPDATE users SET balance = balance + ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement subtractPs = con.prepareStatement(subtractSql);
                 PreparedStatement addPs = con.prepareStatement(addSql)) {

                subtractPs.setBigDecimal(1, amount);
                subtractPs.setLong(2, fromUserId);
                subtractPs.setBigDecimal(3, amount);

                if (subtractPs.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }

                addPs.setBigDecimal(1, amount);
                addPs.setLong(2, toUserId);

                if (addPs.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }

                con.commit();
                return true;

            } catch (Exception e) {
                con.rollback();
                System.err.println("transferBalanceIfEnough error: " + e.getMessage());
                return false;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("transferBalanceIfEnough connection error: " + e.getMessage());
            return false;
        }
    }
}
