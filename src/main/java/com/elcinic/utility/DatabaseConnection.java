package com.elcinic.utility;

import com.elcinic.bootstrap.SchemaMigration;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final Properties PROPS = new Properties();
    private static volatile boolean schemaVerified;

    static {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            PROPS.load(in);
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                envOrProperty("DB_URL", "db.url"),
                envOrProperty("DB_USER", "db.username"),
                envOrProperty("DB_PASSWORD", "db.password")
        );
        ensureSchema(conn);
        return conn;
    }

    private static String envOrProperty(String envKey, String propKey) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return PROPS.getProperty(propKey, "");
    }

    private static void ensureSchema(Connection conn) throws SQLException {
        if (schemaVerified) {
            return;
        }
        synchronized (DatabaseConnection.class) {
            if (!schemaVerified) {
                SchemaMigration.ensureLatest(conn);
                schemaVerified = true;
            }
        }
    }
}
