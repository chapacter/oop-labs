package ru.ssau.tk.avokado.lab2.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Базовый сервлет с обработкой исключений
 */
public abstract class ExceptionHandlingServlet extends HttpServlet {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Метод для обработки исключений в сервлетах
     */
    protected void handleException(Exception e, HttpServletResponse response, Logger logger, String operation) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            if (e instanceof NumberFormatException) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID or value format\", \"message\":\"" + e.getMessage() + "\"}");
                logger.warn("Неверный формат ID или значения при {}: {}", operation, e.getMessage());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Internal server error\", \"message\":\"" + e.getMessage() + "\"}");
                logger.error("Ошибка при обработке {} запроса: {}", operation, e.getMessage(), e);
            }
        } catch (IOException ioException) {
            logger.error("Ошибка при записи ответа об исключении", ioException);
        }
    }
}