package server.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBconnection {
    private static DBconnection instance;
    private final HikariDataSource dataSource;

    private String url = "jdbc:mysql://2gHYcGAmktnRtig.root:tMk8jhH0IqFvkJq5@gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/Auction_System?sslMode=VERIFY_IDENTITY";
    private String user = "2gHYcGAmktnRtig.root";
    private String pass = "tMk8jhH0IqFvkJq5";

    private DBconnection() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);

        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);
    }
    public static synchronized DBconnection getInstance() {
        if (instance == null) {
            instance = new DBconnection();
        }
        return instance;
    }
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}