package server.connection;

import server.database.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDBconection {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getInstance().getConnection();
            System.out.println("Database connected");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}