package server.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBconnection {
    private static DBconnection instance;
    private final HikariDataSource dataSource;

//    Database Local
//    private String url = "jdbc:mysql://localhost:3307/new_db";
//    private String user = "root";
//    private String pass = "123456";

    private String url = "jdbc:mysql://4Fh6tercQZ88ojp.root:Pcw7mel45GAdVwvl@gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/Bidding?sslMode=VERIFY_IDENTITY";
    private String user = "4Fh6tercQZ88ojp.root";
    private String pass = "Pcw7mel45GAdVwvl";


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