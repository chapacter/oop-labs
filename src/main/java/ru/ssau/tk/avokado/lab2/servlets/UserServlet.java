package ru.ssau.tk.avokado.lab2.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.dto.PageResponse;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.dto.request.CreateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.request.UpdateUserRequest;
import ru.ssau.tk.avokado.lab2.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/users/*")
public class UserServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class.getName());
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Получаем аутентифицированного пользователя из атрибута
        UserDto authenticatedUser = (UserDto) request.getAttribute("authenticatedUser");

        try {
            // Проверяем, есть ли у пользователя роль ADMIN
            if ((pathInfo == null || pathInfo.equals("/")) && !authenticatedUser.hasRole("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: only admins can list users\"}");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/users - получить страницу пользователей с поддержкой пагинации и поиска
                String pageParam = request.getParameter("page");
                String sizeParam = request.getParameter("size");
                String nameParam = request.getParameter("name");

                int page = pageParam != null ? Integer.parseInt(pageParam) : 0;
                int size = sizeParam != null ? Integer.parseInt(sizeParam) : 20;

                // Если размер равен 0, используем стандартный размер
                if (size <= 0) size = 20;

                // Проверяем, что page не отрицательный
                if (page < 0) page = 0;

                List<UserDto> users;
                long totalElements;

                if (nameParam != null && !nameParam.isEmpty()) {
                    // Поиск по имени (contains)
                    users = userService.findByNameContaining(nameParam);
                    totalElements = users.size();
                    // Применяем пагинацию вручную, т.к. DAO не поддерживает пагинацию
                    int startIndex = page * size;
                    if (startIndex < users.size()) {
                        int endIndex = Math.min(startIndex + size, users.size());
                        users = users.subList(startIndex, endIndex);
                    } else {
                        users = List.of();
                    }
                } else {
                    // Получаем всех пользователей
                    users = userService.findAll();
                    totalElements = users.size();
                    // Применяем пагинацию вручную
                    int startIndex = page * size;
                    if (startIndex < users.size()) {
                        int endIndex = Math.min(startIndex + size, users.size());
                        users = users.subList(startIndex, endIndex);
                    } else {
                        users = List.of();
                    }
                }

                PageResponse<UserDto> pageResponse = new PageResponse<>(users, page, size, totalElements);
                logger.info("Получена страница пользователей: {} из {}, всего элементов: {}",
                        users.size(), size, totalElements);
                out.print(objectMapper.writeValueAsString(pageResponse));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    // GET /api/users/{id}
                    Long id = Long.parseLong(pathParts[1]);

                    // Пользователь может получить только свои данные или админ может получить любые
                    if (!authenticatedUser.hasRole("ADMIN") && !authenticatedUser.getId().equals(id)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\":\"Access denied: can only access own data\"}");
                        return;
                    }

                    Optional<UserDto> user = userService.findById(id);
                    if (user.isPresent()) {
                        logger.info("Получен пользователь с ID: {}", id);
                        out.print(objectMapper.writeValueAsString(user.get()));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"User not found\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid path\"}");
                }
            }
        } catch (Exception e) {
            handleException(e, response, logger, "GET");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Получаем аутентифицированного пользователя из атрибута
        UserDto authenticatedUser = (UserDto) request.getAttribute("authenticatedUser");

        try {
            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            CreateUserRequest createUserRequest = objectMapper.readValue(buffer.toString(), CreateUserRequest.class);

            // Проверяем, существует ли уже пользователь с таким именем
            List<UserDto> existingUsers = userService.findByNameContaining(createUserRequest.getName());
            for (UserDto user : existingUsers) {
                if (user.getName().equals(createUserRequest.getName())) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"error\":\"User with this name already exists\"}");
                    logger.warn("Attempt to create user with existing name: {}", createUserRequest.getName());
                    return;
                }
            }

            // Проверяем, является ли запрос на создание администратора
            boolean isAdminCreation = authenticatedUser != null && authenticatedUser.hasRole("ADMIN");

            // Если аутентифицированный пользователь не админ, то можно создать только обычного пользователя
            Integer accessLvl = createUserRequest.getAccessLvl();
            if (!isAdminCreation) {
                // Обычные пользователи могут создавать только обычных пользователей
                if (accessLvl != null && accessLvl == 1) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Access denied: only admins can create admins\"}");
                    logger.warn("Non-admin user {} attempted to create admin user",
                            authenticatedUser != null ? authenticatedUser.getName() : "unauthenticated");
                    return;
                }
                // Для обычного пользователя устанавливаем уровень доступа по умолчанию
                accessLvl = 0;
            }

            // Создаем UserDto из CreateUserRequest
            UserDto user = new UserDto(
                    createUserRequest.getName(),
                    accessLvl,
                    createUserRequest.getPassword() // используем password из запроса как passwordHash
            );

            // Установка роли по умолчанию - USER, если не указан уровень доступа или он не равен 1
            if (accessLvl == null || accessLvl != 1) {
                user.addRole("USER");
            } else {
                user.addRole("ADMIN");
            }

            Long id = userService.save(user);
            if (id != null) {
                user.setId(id);
                // Обновляем роли из базы данных
                user.setRoles(userService.getUserRoles(id));
                logger.info("Создан пользователь с ID: {} и именем: {}", id, user.getName());
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(user));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create user\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "POST");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Получаем аутентифицированного пользователя из атрибута
        UserDto authenticatedUser = (UserDto) request.getAttribute("authenticatedUser");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Path info is required\"}");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid path format\"}");
                return;
            }

            Long id = Long.parseLong(pathParts[1]);

            // Пользователь может обновлять только свои данные или админ может обновлять любые
            if (!authenticatedUser.hasRole("ADMIN") && !authenticatedUser.getId().equals(id)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only update own data\"}");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            UpdateUserRequest updateUserRequest = objectMapper.readValue(buffer.toString(), UpdateUserRequest.class);

            // Проверяем, пытается ли пользователь изменить уровень доступа
            if (updateUserRequest.getAccessLvl() != null && !authenticatedUser.hasRole("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: only admins can change access levels\"}");
                return;
            }

            // Получаем существующего пользователя
            Optional<UserDto> existingUserOpt = userService.findById(id);
            if (!existingUserOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"User not found\"}");
                return;
            }

            UserDto existingUser = existingUserOpt.get();

            // Обновляем только указанные поля
            if (updateUserRequest.getName() != null) {
                existingUser.setName(updateUserRequest.getName());
            }

            // Администратор может изменить уровень доступа любого пользователя
            if (updateUserRequest.getAccessLvl() != null && authenticatedUser.hasRole("ADMIN")) {
                existingUser.setAccessLvl(updateUserRequest.getAccessLvl());
                // Обновляем роли на основе уровня доступа
                if (updateUserRequest.getAccessLvl() == 1) {
                    existingUser.addRole("ADMIN");
                    existingUser.removeRole("USER");
                } else {
                    existingUser.addRole("USER");
                    existingUser.removeRole("ADMIN");
                }
                logger.info("User {} changed access level of user {} to {}",
                        authenticatedUser.getName(), existingUser.getName(),
                        updateUserRequest.getAccessLvl() == 1 ? "ADMIN" : "USER");
            } else if (updateUserRequest.getAccessLvl() != null) {
                // Если обычный пользователь пытается изменить уровень доступа (даже свой), это запрещено
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: cannot change access level\"}");
                return;
            }

            boolean updated = userService.update(existingUser);
            if (updated) {
                logger.info("Обновлен пользователь с ID: {}", id);
                out.print(objectMapper.writeValueAsString(existingUser));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"User not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "PUT");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Получаем аутентифицированного пользователя из атрибута
        UserDto authenticatedUser = (UserDto) request.getAttribute("authenticatedUser");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Path info is required\"}");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid path format\"}");
                return;
            }

            Long id = Long.parseLong(pathParts[1]);

            // Только администратор может удалять пользователей
            if (!authenticatedUser.hasRole("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: only admins can delete users\"}");
                return;
            }

            // Нельзя удалить самого себя
            if (authenticatedUser.getId().equals(id)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: cannot delete yourself\"}");
                return;
            }

            boolean deleted = userService.delete(id);
            if (deleted) {
                logger.info("Удален пользователь с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"User not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "DELETE");
        }
    }
}