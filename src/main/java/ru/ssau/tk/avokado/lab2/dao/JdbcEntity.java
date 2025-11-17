package ru.ssau.tk.avokado.lab2.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class JdbcEntity {
    private static final Logger log = LoggerFactory.getLogger(JdbcEntity.class);
    private static final Properties props = new Properties();

    static {
        try (InputStream is = JdbcEntity.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (is == null) throw new RuntimeException("database.properties not found");
            props.load(is);
        } catch (Exception e) {
            log.error("Failed to load database.properties", e);
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws Exception {
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");
        return DriverManager.getConnection(url, user, pass);
    }
}