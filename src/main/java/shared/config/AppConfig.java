package shared.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("Không tìm thấy file config.properties");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file config.properties", e);
        }
    }

    public static String getServerHost() { return properties.getProperty("server.host"); }

    public static int getServerPort() { return Integer.parseInt(properties.getProperty("server.port")); }

    public static String getDbUrl() { return properties.getProperty("db.url"); }

    public static String getDbUsername() { return properties.getProperty("db.username"); }

    public static String getDbPassword() { return properties.getProperty("db.password"); }

    public static String getDbDriver() { return properties.getProperty("db.driver"); }
}