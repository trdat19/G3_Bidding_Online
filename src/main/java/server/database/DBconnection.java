package server.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBconnection {
    private static DBconnection instance;
    private final HikariDataSource dataSource;

    private String url = "jdbc:mysql://127.0.0.1:3306/local_railway";
    private String user = "root";
    private String pass = "mysql2007";

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