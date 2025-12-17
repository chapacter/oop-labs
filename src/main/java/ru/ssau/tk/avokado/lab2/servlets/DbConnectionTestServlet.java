package ru.ssau.tk.avokado.lab2.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.ssau.tk.avokado.lab2.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet("/api/test-db-connection")
public class DbConnectionTestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                // Попробуем выполнить простой SQL-запрос для проверки подключения
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT 1");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"status\":\"success\",\"message\":\"Database connection established and functional\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Failed to establish database connection\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\",\"details\":\"" + e.getClass().getName() + "\"}");
        }
    }
}