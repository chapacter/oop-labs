package ru.ssau.tk.avokado.lab2.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Path info is required");
            return;
        }

        switch (pathInfo) {
            case "/register":
                register(req, resp);
                break;
            case "/login":
                login(req, resp);
                break;
            default:
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                break;
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            Map<String, Object> requestMap = objectMapper.readValue(requestBody.toString(), Map.class);

            String username = (String) requestMap.get("username");
            String password = (String) requestMap.get("password");

            if (username == null || username.trim().isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Username is required");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Password is required");
                return;
            }

            // Проверяем, существует ли уже пользователь
            if (userService.existsByUsername(username)) {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "User already exists");
                return;
            }

            // Создаем нового пользователя
            UserDto user = new UserDto(username, 0, password);
            // Добавляем роль USER по умолчанию
            user.addRole("USER");

            Long userId = userService.save(user);

            if (userId != null) {
                user.setId(userId);
                // Устанавливаем роли для возвращаемого объекта
                user.setRoles(userService.getUserRoles(userId));

                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(objectMapper.writeValueAsString(user));
                logger.info("User registered successfully: {}", username);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register user");
            }
        } catch (Exception e) {
            logger.error("Error during registration", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            Map<String, Object> requestMap = objectMapper.readValue(requestBody.toString(), Map.class);

            String username = (String) requestMap.get("username");
            String password = (String) requestMap.get("password");

            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required");
                return;
            }

            // В текущей реализации пароль хранится в открытом виде, но в реальном приложении нужно использовать хеширование
            UserDto user = new UserDto(username, 0, password);

            // Аутентификация пользователя
            if (userService.authenticate(username, password)) {
                Optional<UserDto> authenticatedUser = userService.findByUsernameAndPassword(username, password);

                if (authenticatedUser.isPresent()) {
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(objectMapper.writeValueAsString(authenticatedUser.get()));
                    logger.info("User logged in successfully: {}", username);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
                }
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            }
        } catch (Exception e) {
            logger.error("Error during login", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);

        resp.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}