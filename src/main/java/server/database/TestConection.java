package server.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConection {
    public static void main(String[] args) {
        try {
            Connection con = server.database.DBconnection.getInstance().getConnection();
            System.out.println("Database connected");
        } catch (SQLException e)
        {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}