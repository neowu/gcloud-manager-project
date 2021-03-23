package core.infra.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * @author neo
 */
public class MySQLClient implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(MySQLClient.class);
    private final Connection connection;

    public MySQLClient(String ip, String user, String password) throws SQLException {
        try {
            Driver driver = Class.forName("com.mysql.cj.jdbc.Driver").asSubclass(Driver.class).getDeclaredConstructor().newInstance();
            var properties = new Properties();
            properties.setProperty("user", user);
            properties.setProperty("password", password);
            properties.setProperty("useSSL", "false");
            logger.info("connect to mysql, ip={}", ip);
            connection = driver.connect("jdbc:mysql://" + ip, properties);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    public void createDB(String db) throws SQLException {
        logger.info("create db, db={}", db);
        String sql = String.format("CREATE DATABASE IF NOT EXISTS %s CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", db);
        try (var statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    public void createUser(String user, String password, String db, List<String> privileges) throws SQLException {
        logger.info("create user, user={}", user);
        try (var statement = connection.createStatement()) {
            statement.addBatch(String.format("CREATE USER IF NOT EXISTS '%s'@'%%'", user));
            statement.addBatch(String.format("ALTER USER '%s'@'%%' IDENTIFIED BY '%s'", user, password));
            statement.addBatch(String.format("GRANT %s ON %s.* TO '%s'@'%%'", String.join(", ", privileges), db, user));
            statement.executeBatch();
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            logger.info("close mysql connection");
            try {
                connection.close();
            } catch (SQLException e) {
                throw new Error(e);
            }
        }
    }
}
