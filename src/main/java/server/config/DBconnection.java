package server.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DBconnectio {
    private static final String URL = "jdbc:mysql://localhost:3306/Auction_System";
    private static final String USER = "root";
    private static final String PASSWORD = "Dat56789@"; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
public class DBconnection {
    public static void main(String[] args) {
        try {
            Connection con = DBconnectio.getConnection();
            System.out.println("connectioned");
        } catch (SQLException e) {
            System.err.println("ERROR ");
        }
        System.out.println("thu");
    }
}


