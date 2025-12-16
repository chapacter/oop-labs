package ru.ssau.tk.avokado.lab2.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.dao.FunctionDao;
import ru.ssau.tk.avokado.lab2.dao.JdbcFunctionDao;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.PageResponse;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.dto.request.CreateFunctionRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/functions/*")
public class FunctionServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionServlet.class.getName());
    private final FunctionDao functionDao = new JdbcFunctionDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Получаем аутентифицированного пользователя из атрибута
        UserDto authenticatedUser = (UserDto) request.getAttribute("authenticatedUser");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/functions - получить страницу функций с поддержкой пагинации и поиска
                String pageParam = request.getParameter("page");
                String sizeParam = request.getParameter("size");
                String nameParam = request.getParameter("name");
                String userIdParam = request.getParameter("userId");
                String formatParam = request.getParameter("format");
                String withPointsParam = request.getParameter("withPoints");

                int page = pageParam != null ? Integer.parseInt(pageParam) : 0;
                int size = sizeParam != null ? Integer.parseInt(sizeParam) : 20;

                // Если размер равен 0, используем стандартный размер
                if (size <= 0) size = 20;

                // Проверяем, что page не отрицательный
                if (page < 0) page = 0;

                List<FunctionDto> functions;
                long totalElements;

                // Применяем фильтры
                if (nameParam != null && !nameParam.isEmpty()) {
                    functions = functionDao.findByNameContaining(nameParam);
                    totalElements = functions.size();
                    // Применяем пагинацию вручную
                    int startIndex = page * size;
                    if (startIndex < functions.size()) {
                        int endIndex = Math.min(startIndex + size, functions.size());
                        functions = functions.subList(startIndex, endIndex);
                    } else {
                        functions = List.of();
                    }
                } else if (userIdParam != null && !userIdParam.isEmpty()) {
                    Long userId = Long.parseLong(userIdParam);
                    // Пользователь может получить только свои функции или админ может получить чужие
                    if (!authenticatedUser.hasRole("ADMIN") && !authenticatedUser.getId().equals(userId)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\":\"Access denied: can only access own functions\"}");
                        return;
                    }
                    functions = functionDao.findByUserId(userId);
                    totalElements = functions.size();
                    // Применяем пагинацию вручную
                    int startIndex = page * size;
                    if (startIndex < functions.size()) {
                        int endIndex = Math.min(startIndex + size, functions.size());
                        functions = functions.subList(startIndex, endIndex);
                    } else {
                        functions = List.of();
                    }
                } else {
                    // Обычные пользователи могут видеть только свои функции, администраторы - все
                    if (authenticatedUser.hasRole("ADMIN")) {
                        // Администратор видит все функции
                        functions = functionDao.findAll();
                        totalElements = functions.size();
                        // Применяем пагинацию вручную
                        int startIndex = page * size;
                        if (startIndex < functions.size()) {
                            int endIndex = Math.min(startIndex + size, functions.size());
                            functions = functions.subList(startIndex, endIndex);
                        } else {
                            functions = List.of();
                        }
                    } else {
                        // Обычный пользователь видит только свои функции
                        functions = functionDao.findByUserId(authenticatedUser.getId());
                        totalElements = functions.size();
                        // Применяем пагинацию вручную
                        int startIndex = page * size;
                        if (startIndex < functions.size()) {
                            int endIndex = Math.min(startIndex + size, functions.size());
                            functions = functions.subList(startIndex, endIndex);
                        } else {
                            functions = List.of();
                        }
                    }
                }

                PageResponse<FunctionDto> pageResponse = new PageResponse<>(functions, page, size, totalElements);
                logger.info("Получена страница функций: {} из {}, всего элементов: {}",
                        functions.size(), size, totalElements);
                out.print(objectMapper.writeValueAsString(pageResponse));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    // GET /api/functions/{id}
                    Long id = Long.parseLong(pathParts[1]);
                    Optional<FunctionDto> function = functionDao.findById(id);
                    if (function.isPresent()) {
                        FunctionDto func = function.get();
                        // Пользователь может получить только свои функции или админ может получить любые
                        if (!authenticatedUser.hasRole("ADMIN") && !func.getUserId().equals(authenticatedUser.getId())) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            out.print("{\"error\":\"Access denied: can only access own functions\"}");
                            return;
                        }
                        logger.info("Получена функция с ID: {}", id);
                        out.print(objectMapper.writeValueAsString(function.get()));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Function not found\"}");
                    }
                } else if (pathParts.length == 3 && pathParts[1].equals("by-user")) {
                    // GET /api/functions/by-user/{userId}
                    Long userId = Long.parseLong(pathParts[2]);
                    // Пользователь может получить только свои функции или админ может получить чужие
                    if (!authenticatedUser.hasRole("ADMIN") && !authenticatedUser.getId().equals(userId)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\":\"Access denied: can only access own functions\"}");
                        return;
                    }
                    List<FunctionDto> functions = functionDao.findByUserId(userId);
                    logger.info("Получено {} функций для пользователя ID: {}", functions.size(), userId);
                    out.print(objectMapper.writeValueAsString(functions));
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

            CreateFunctionRequest createFunctionRequest = objectMapper.readValue(buffer.toString(), CreateFunctionRequest.class);

            // Создаем FunctionDto из CreateFunctionRequest, используя ID аутентифицированного пользователя
            FunctionDto function = new FunctionDto(
                    authenticatedUser.getId(), // Всегда используем ID аутентифицированного пользователя
                    createFunctionRequest.getName(),
                    createFunctionRequest.getFormat(),
                    createFunctionRequest.getFuncResult()
            );

            Long id = functionDao.save(function);
            if (id != null) {
                function.setId(id);
                logger.info("Создана функция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(function));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create function\"}");
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

            // Проверяем, является ли пользователь владельцем функции или администратором
            Optional<FunctionDto> existingFunctionOpt = functionDao.findById(id);
            if (existingFunctionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto existingFunction = existingFunctionOpt.get();
            if (!authenticatedUser.hasRole("ADMIN") && !existingFunction.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only update own functions\"}");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            FunctionDto function = objectMapper.readValue(buffer.toString(), FunctionDto.class);
            // Устанавливаем ID и ID пользователя из существующей функции для безопасности
            function.setId(id);
            function.setUserId(existingFunction.getUserId());

            boolean updated = functionDao.update(function);
            if (updated) {
                logger.info("Обновлена функция с ID: {}", id);
                out.print(objectMapper.writeValueAsString(function));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
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

            // Проверяем, является ли пользователь владельцем функции или администратором
            Optional<FunctionDto> existingFunctionOpt = functionDao.findById(id);
            if (existingFunctionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto existingFunction = existingFunctionOpt.get();
            if (!authenticatedUser.hasRole("ADMIN") && !existingFunction.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only delete own functions\"}");
                return;
            }

            boolean deleted = functionDao.delete(id);
            if (deleted) {
                logger.info("Удалена функция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "DELETE");
        }
    }
}