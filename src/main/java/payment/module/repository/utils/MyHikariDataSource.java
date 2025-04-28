package payment.module.repository.utils;

import com.zaxxer.hikari.HikariConfig;
import payment.module.util.ConfigReader;
import payment.module.util.LogManager;

import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;

public class MyHikariDataSource {
    private static com.zaxxer.hikari.HikariDataSource dataSource;

    public static com.zaxxer.hikari.HikariDataSource getDataSource(){
        if(dataSource == null){
            Properties props = new Properties();

            String DB_URL = ConfigReader.getStringValue("DB_URL");
            String DB_LOGIN = ConfigReader.getStringValue("DB_LOGIN");
            String DB_PASSWORD = ConfigReader.getStringValue("DB_PASSWORD");

            props.setProperty("jdbcUrl", DB_URL);
            props.setProperty("dataSource.user", DB_LOGIN);
            props.setProperty("dataSource.password", DB_PASSWORD);
            props.put("dataSource.logWriter", new PrintWriter(System.out));

            HikariConfig config = new HikariConfig(props);
            dataSource = new com.zaxxer.hikari.HikariDataSource(config);
        }
        return dataSource;
    }

    public static void destroy(){
        if(dataSource != null) {
            dataSource.close();
        } else {
            LogManager.logException(new NullPointerException("tried to destroy not existing HikariCP"), Level.WARNING);
        }
    }

}
