package ru.ssau.tk.avokado.lab2.bench;

import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BenchmarkRunner {

    public static void main(String[] args) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("benchdb")
                .withUsername("bench")
                .withPassword("bench");

        postgres.start();

        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Выполняем инициализацию базы данных
            initializeDatabase(connection);

            // Запускаем JDBC бенчмарки
            BenchmarkService benchmarkService = new BenchmarkService(connection);
            benchmarkService.run();

            SortingBenchmark sortingBenchmark = new SortingBenchmark(connection);
            sortingBenchmark.run();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            postgres.stop();
        }
    }

    private static void initializeDatabase(Connection connection) throws SQLException {
        // Инициализация базы данных - создание таблиц
        try (Statement statement = connection.createStatement()) {
            // Создаем таблицы, если они не существуют
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "password_hash VARCHAR(25), " +
                    "access_lvl INTEGER" +
                    ")");

            statement.execute("CREATE TABLE IF NOT EXISTS functions (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "user_id BIGINT, " +
                    "func_result VARCHAR(255), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")");

            statement.execute("CREATE TABLE IF NOT EXISTS points (" +
                    "id SERIAL PRIMARY KEY, " +
                    "x DOUBLE PRECISION, " +
                    "y DOUBLE PRECISION, " +
                    "index_in_function INTEGER, " +
                    "function_id BIGINT, " +
                    "FOREIGN KEY (function_id) REFERENCES functions(id)" +
                    ")");
        }
    }
}