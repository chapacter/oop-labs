package ru.ssau.tk.avokado.lab2.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.Role;
import ru.ssau.tk.avokado.lab2.dao.FunctionDao;
import ru.ssau.tk.avokado.lab2.dao.JdbcFunctionDao;
import ru.ssau.tk.avokado.lab2.dao.JdbcPointDao;
import ru.ssau.tk.avokado.lab2.dao.PointDao;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.PageResponse;
import ru.ssau.tk.avokado.lab2.dto.PointDto;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.dto.request.CreatePointRequest;
import ru.ssau.tk.avokado.lab2.dto.request.UpdatePointRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/points/*")
public class PointServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(PointServlet.class.getName());
    private final PointDao pointDao = new JdbcPointDao();
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
                // GET /api/points - получить страницу точек с поддержкой пагинации и поиска
                String pageParam = request.getParameter("page");
                String sizeParam = request.getParameter("size");
                String functionIdParam = request.getParameter("functionId");

                int page = pageParam != null ? Integer.parseInt(pageParam) : 0;
                int size = sizeParam != null ? Integer.parseInt(sizeParam) : 20;

                // Если размер равен 0, используем стандартный размер
                if (size <= 0) size = 20;

                // Проверяем, что page не отрицательный
                if (page < 0) page = 0;

                List<PointDto> points;
                long totalElements;

                // Применяем фильтры
                if (functionIdParam != null && !functionIdParam.isEmpty()) {
                    Long functionId = Long.parseLong(functionIdParam);
                    // Проверяем, является ли пользователь владельцем функции или администратором
                    Optional<FunctionDto> functionOpt = functionDao.findById(functionId);
                    if (functionOpt.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Function not found\"}");
                        return;
                    }

                    FunctionDto function = functionOpt.get();
                    if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\":\"Access denied: can only access points of own functions\"}");
                        return;
                    }

                    points = pointDao.findByFunctionId(functionId);
                    totalElements = points.size();
                    // Применяем пагинацию вручную
                    int startIndex = page * size;
                    if (startIndex < points.size()) {
                        int endIndex = Math.min(startIndex + size, points.size());
                        points = points.subList(startIndex, endIndex);
                    } else {
                        points = List.of();
                    }
                } else {
                    // Обычные пользователи могут видеть только точки своих функций, администраторы - все
                    if (authenticatedUser.getRole() == Role.ADMIN) {
                        // Администратор видит все точки
                        points = pointDao.findAll();
                        totalElements = points.size();
                        // Применяем пагинацию вручную
                        int startIndex = page * size;
                        if (startIndex < points.size()) {
                            int endIndex = Math.min(startIndex + size, points.size());
                            points = points.subList(startIndex, endIndex);
                        } else {
                            points = List.of();
                        }
                    } else {
                        // Обычный пользователь видит только точки своих функций
                        points = pointDao.findByUserId(authenticatedUser.getId());
                        totalElements = points.size();
                        // Применяем пагинацию вручную
                        int startIndex = page * size;
                        if (startIndex < points.size()) {
                            int endIndex = Math.min(startIndex + size, points.size());
                            points = points.subList(startIndex, endIndex);
                        } else {
                            points = List.of();
                        }
                    }
                }

                PageResponse<PointDto> pageResponse = new PageResponse<>(points, page, size, totalElements);
                logger.info("Получена страница точек: {} из {}, всего элементов: {}",
                        points.size(), size, totalElements);
                out.print(objectMapper.writeValueAsString(pageResponse));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    // GET /api/points/{id}
                    Long id = Long.parseLong(pathParts[1]);
                    Optional<PointDto> point = pointDao.findById(id);
                    if (point.isPresent()) {
                        PointDto pointDto = point.get();
                        // Проверяем, является ли пользователь владельцем функции точки или администратором
                        Optional<FunctionDto> functionOpt = functionDao.findById(pointDto.getFunctionId());
                        if (functionOpt.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"error\":\"Function not found\"}");
                            return;
                        }

                        FunctionDto function = functionOpt.get();
                        if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            out.print("{\"error\":\"Access denied: can only access points of own functions\"}");
                            return;
                        }

                        logger.info("Получена точка с ID: {}", id);
                        out.print(objectMapper.writeValueAsString(point.get()));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Point not found\"}");
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

            CreatePointRequest createPointRequest = objectMapper.readValue(buffer.toString(), CreatePointRequest.class);

            // Проверяем, является ли пользователь владельцем функции или администратором
            Optional<FunctionDto> functionOpt = functionDao.findById(createPointRequest.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only add points to own functions\"}");
                return;
            }

            // Создаем PointDto из CreatePointRequest
            PointDto point = new PointDto(
                    createPointRequest.getFunctionId(),
                    createPointRequest.getX(),
                    createPointRequest.getY(),
                    createPointRequest.getIndexInFunction()
            );

            Long id = pointDao.save(point);
            if (id != null) {
                point.setId(id);
                logger.info("Создана точка с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(point));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create point\"}");
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

            // Получаем существующую точку
            Optional<PointDto> existingPointOpt = pointDao.findById(id);
            if (!existingPointOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Point not found\"}");
                return;
            }

            PointDto existingPoint = existingPointOpt.get();

            // Проверяем, является ли пользователь владельцем функции точки или администратором
            Optional<FunctionDto> functionOpt = functionDao.findById(existingPoint.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only update points of own functions\"}");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            UpdatePointRequest updatePointRequest = objectMapper.readValue(buffer.toString(), UpdatePointRequest.class);

            // Обновляем поля, но не позволяем изменить functionId, если только пользователь не админ
            if (updatePointRequest.getFunctionId() != null && !authenticatedUser.getRole().equals(Role.ADMIN)) {
                // Проверяем, является ли новая функция принадлежащей пользователю
                Optional<FunctionDto> newFunctionOpt = functionDao.findById(updatePointRequest.getFunctionId());
                if (newFunctionOpt.isEmpty() || !newFunctionOpt.get().getUserId().equals(authenticatedUser.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Access denied: can only move point to own function\"}");
                    return;
                }
                existingPoint.setFunctionId(updatePointRequest.getFunctionId());
            } else if (updatePointRequest.getFunctionId() != null && authenticatedUser.getRole().equals(Role.ADMIN)) {
                existingPoint.setFunctionId(updatePointRequest.getFunctionId());
            }

            if (updatePointRequest.getX() != null) {
                existingPoint.setX(updatePointRequest.getX());
            }
            if (updatePointRequest.getY() != null) {
                existingPoint.setY(updatePointRequest.getY());
            }
            if (updatePointRequest.getPointIndex() != null) {
                existingPoint.setPointIndex(updatePointRequest.getPointIndex());
            }

            boolean updated = pointDao.update(existingPoint);
            if (updated) {
                logger.info("Обновлена точка с ID: {}", id);
                out.print(objectMapper.writeValueAsString(existingPoint));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Point not found\"}");
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

            // Проверяем, является ли пользователь владельцем функции точки или администратором
            Optional<PointDto> existingPointOpt = pointDao.findById(id);
            if (!existingPointOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Point not found\"}");
                return;
            }

            PointDto existingPoint = existingPointOpt.get();
            Optional<FunctionDto> functionOpt = functionDao.findById(existingPoint.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only delete points of own functions\"}");
                return;
            }

            boolean deleted = pointDao.delete(id);
            if (deleted) {
                logger.info("Удалена точка с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Point not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "DELETE");
        }
    }
}