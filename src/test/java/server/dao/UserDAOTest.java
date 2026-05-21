package server.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.database.DBconnection;
import server.model.user.Admin;
import server.model.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@ExtendWith(MockitoExtension.class)
public class UserDAOTest {

    @Mock private DBconnection dbConnection;
    @Mock private Connection connection;
    @Mock private PreparedStatement ps;
    @Mock private ResultSet rs;
    @Mock private ResultSet generatedKeys;

    private UserDAO userDAO;

    //--------------HELPER tạo user mẫu-----------------
    private User buildSampleUser() {
        User user = new Admin();
        return user;
    }

    @BeforeEach
    public void setup() {

    }
}