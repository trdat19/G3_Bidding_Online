package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBconection {

//    Database Local
//    private static final String URL = "jdbc:mysql://localhost:3307/new_db";
//    private static final String USER = "root";
//    private static final String PASSWORD = "123456";

    private static final String URL = "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/new_db";
    private static final String USER = "2RZiMXNpgzAb2Zf.root";
    private static final String PASSWORD = "QUGAicy63NlmSGfY";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}