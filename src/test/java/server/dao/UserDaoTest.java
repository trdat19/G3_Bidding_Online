package server.dao;

import org.junit.jupiter.api.*;

import server.model.user.Bidder;
import server.model.user.User;
import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;


public class UserDaoTest {
    private Connection connection;
    private UserDao userDAO;

    @BeforeEach
    void setUp() throws SQLException {
        connection = TestDBconection.getConnection();
        userDAO = new UserDao(connection);

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM users");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM users");
        }

        connection.close();
    }

    @Test
    void insertUser_ShouldSaveUserToDatabase() throws SQLException {
        User user = new Bidder();
        user.setUsername("Dat");
        user.setPassword("123456789");
        user.setFullname("Dang Tien Dat");
        user.setEmail("hihi@gmail.com");
        user.setRole(UserRole.BIDDER);
        user.setStatus(UserStatus.ACTIVE);

        boolean result = userDAO.insertUser(user);

        assertTrue(result);
    }

    @Test
    void insertDuplicateUsername_ShouldThrowException() throws SQLException {
        User user1 = new Bidder();
        user1.setUsername("vanh_bu");
        user1.setPassword("123456");
        user1.setFullname("Viet anh nguyễn");
        user1.setEmail("vanh@gmail.com");
        user1.setRole(UserRole.BIDDER);
        user1.setStatus(UserStatus.ACTIVE);

        User user2 = new Bidder();
        user2.setUsername("dat_bacninh");
        user2.setPassword("1245");
        user2.setFullname("Thành Đạt");
        user2.setEmail("Dattruong@vnu.edu.vn");
        user2.setRole(UserRole.BIDDER);
        user2.setStatus(UserStatus.ACTIVE);

        userDAO.insertUser(user1);

        assertThrows(SQLException.class, () -> {
            userDAO.insertUser(user2);
        });
    }
}

