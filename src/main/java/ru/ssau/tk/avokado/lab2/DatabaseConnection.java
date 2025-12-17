package ru.ssau.tk.avokado.lab2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "db";
    private static final String DB_PORT = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "avokado-bd";
    private static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "postgres";

    private static final String URL = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load PostgreSQL driver", e);
        }

        PROPERTIES.setProperty("user", DB_USER);
        PROPERTIES.setProperty("password", DB_PASSWORD);
        PROPERTIES.setProperty("ssl", "false");
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, PROPERTIES);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database", e);
        }
    }
}