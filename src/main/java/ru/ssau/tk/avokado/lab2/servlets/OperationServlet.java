package ru.ssau.tk.avokado.lab2.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.Role;
import ru.ssau.tk.avokado.lab2.dao.JdbcOperationDao;
import ru.ssau.tk.avokado.lab2.dao.OperationDao;
import ru.ssau.tk.avokado.lab2.dto.OperationDto;
import ru.ssau.tk.avokado.lab2.dto.PageResponse;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.dto.request.CreateOperationRequest;
import ru.ssau.tk.avokado.lab2.dto.request.UpdateOperationRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/operations/*")
public class OperationServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(OperationServlet.class.getName());
    private final OperationDao operationDao = new JdbcOperationDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Получаем аутентифицированного пользователя из атрибута
        UserDto authenticatedUser = (UserDto) request.getAttribute("authenticatedUser");

        try {
            // Только администраторы могут получать информацию об операциях
            if (authenticatedUser.getRole() != Role.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: only admins can access operations\"}");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/operations - получить страницу операций с поддержкой пагинации
                String pageParam = request.getParameter("page");
                String sizeParam = request.getParameter("size");

                int page = pageParam != null ? Integer.parseInt(pageParam) : 0;
                int size = sizeParam != null ? Integer.parseInt(sizeParam) : 20;

                // Если размер равен 0, используем стандартный размер
                if (size <= 0) size = 20;

                // Проверяем, что page не отрицательный
                if (page < 0) page = 0;

                List<OperationDto> operations = operationDao.findAll();
                long totalElements = operations.size();

                // Применяем пагинацию вручную
                int startIndex = page * size;
                if (startIndex < operations.size()) {
                    int endIndex = Math.min(startIndex + size, operations.size());
                    operations = operations.subList(startIndex, endIndex);
                } else {
                    operations = List.of();
                }

                PageResponse<OperationDto> pageResponse = new PageResponse<>(operations, page, size, totalElements);
                logger.info("Получена страница операций: {} из {}, всего элементов: {}",
                        operations.size(), size, totalElements);
                out.print(objectMapper.writeValueAsString(pageResponse));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    // GET /api/operations/{id}
                    Long id = Long.parseLong(pathParts[1]);
                    Optional<OperationDto> operation = operationDao.findById(id);
                    if (operation.isPresent()) {
                        logger.info("Получена операция с ID: {}", id);
                        out.print(objectMapper.writeValueAsString(operation.get()));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Operation not found\"}");
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
            // Только администраторы могут создавать операции
            if (authenticatedUser.getRole() != Role.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: only admins can create operations\"}");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            CreateOperationRequest createOperationRequest = objectMapper.readValue(buffer.toString(), CreateOperationRequest.class);

            // Создаем OperationDto из CreateOperationRequest
            OperationDto operation = new OperationDto(
                    createOperationRequest.getName(),
                    createOperationRequest.getDescription()
            );

            Long id = operationDao.save(operation);
            if (id != null) {
                operation.setId(id);
                logger.info("Создана операция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(operation));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create operation\"}");
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
            // Только администраторы могут обновлять операции
            if (authenticatedUser.getRole() != Role.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: only admins can update operations\"}");
                return;
            }

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

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            UpdateOperationRequest updateOperationRequest = objectMapper.readValue(buffer.toString(), UpdateOperationRequest.class);

            // Получаем существующую операцию
            Optional<OperationDto> existingOperationOpt = operationDao.findById(id);
            if (!existingOperationOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Operation not found\"}");
                return;
            }

            OperationDto existingOperation = existingOperationOpt.get();

            // Обновляем поля
            if (updateOperationRequest.getName() != null) {
                existingOperation.setName(updateOperationRequest.getName());
            }
            if (updateOperationRequest.getDescription() != null) {
                existingOperation.setDescription(updateOperationRequest.getDescription());
            }

            boolean updated = operationDao.update(existingOperation);
            if (updated) {
                logger.info("Обновлена операция с ID: {}", id);
                out.print(objectMapper.writeValueAsString(existingOperation));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Operation not found\"}");
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
            // Только администраторы могут удалять операции
            if (authenticatedUser.getRole() != Role.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: only admins can delete operations\"}");
                return;
            }

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

            boolean deleted = operationDao.delete(id);
            if (deleted) {
                logger.info("Удалена операция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Operation not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "DELETE");
        }
    }
}