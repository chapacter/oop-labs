package ru.ssau.tk.avokado.lab2.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.Role;
import ru.ssau.tk.avokado.lab2.dao.*;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.ProcessedFunctionDto;
import ru.ssau.tk.avokado.lab2.dto.ResultValueDto;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.dto.request.CreateResultValueRequest;
import ru.ssau.tk.avokado.lab2.dto.request.UpdateResultValueRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/result-values/*")
public class ResultValueServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(ResultValueServlet.class.getName());
    private final ResultValueDao resultValueDao = new JdbcResultValueDao();
    private final ProcessedFunctionDao processedFunctionDao = new JdbcProcessedFunctionDao();
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
                // GET /api/result-values - получить все результирующие значения
                // Обычные пользователи могут видеть только результирующие значения своих функций, администраторы - все
                List<ResultValueDto> resultValues;
                if (authenticatedUser.getRole() == Role.ADMIN) {
                    resultValues = resultValueDao.findAll();
                } else {
                    // Получаем все функции пользователя, затем обработанные функции для этих функций, а затем результирующие значения
                    List<FunctionDto> userFunctions = functionDao.findByUserId(authenticatedUser.getId());
                    resultValues = new java.util.ArrayList<>();
                    for (FunctionDto function : userFunctions) {
                        List<ProcessedFunctionDto> processedFunctions = processedFunctionDao.findByFunctionId(function.getId());
                        for (ProcessedFunctionDto processedFunction : processedFunctions) {
                            resultValues.addAll(resultValueDao.findByProcessedFunctionId(processedFunction.getId()));
                        }
                    }
                }
                logger.info("Получено {} результирующих значений", resultValues.size());
                out.print(objectMapper.writeValueAsString(resultValues));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    // GET /api/result-values/{id}
                    Long id = Long.parseLong(pathParts[1]);
                    Optional<ResultValueDto> resultValue = resultValueDao.findById(id);
                    if (resultValue.isPresent()) {
                        ResultValueDto rv = resultValue.get();
                        // Проверяем, является ли пользователь владельцем связанной функции или администратором
                        Optional<ProcessedFunctionDto> processedFunctionOpt = processedFunctionDao.findById(rv.getProcessedFunctionId());
                        if (processedFunctionOpt.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"error\":\"Processed function not found\"}");
                            return;
                        }

                        ProcessedFunctionDto processedFunction = processedFunctionOpt.get();
                        Optional<FunctionDto> functionOpt = functionDao.findById(processedFunction.getFunctionId());
                        if (functionOpt.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"error\":\"Function not found\"}");
                            return;
                        }

                        FunctionDto function = functionOpt.get();
                        if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            out.print("{\"error\":\"Access denied: can only access result values of own functions\"}");
                            return;
                        }

                        logger.info("Получено результирующее значение с ID: {}", id);
                        out.print(objectMapper.writeValueAsString(resultValue.get()));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Result value not found\"}");
                    }
                } else if (pathParts.length == 3 && pathParts[1].equals("processed")) {
                    // GET /api/result-values/processed/{processedFunctionId}
                    Long processedFunctionId = Long.parseLong(pathParts[2]);
                    // Проверяем, является ли пользователь владельцем связанной функции или администратором
                    Optional<ProcessedFunctionDto> processedFunctionOpt = processedFunctionDao.findById(processedFunctionId);
                    if (processedFunctionOpt.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Processed function not found\"}");
                        return;
                    }

                    ProcessedFunctionDto processedFunction = processedFunctionOpt.get();
                    Optional<FunctionDto> functionOpt = functionDao.findById(processedFunction.getFunctionId());
                    if (functionOpt.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Function not found\"}");
                        return;
                    }

                    FunctionDto function = functionOpt.get();
                    if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\":\"Access denied: can only access result values of own functions\"}");
                        return;
                    }

                    List<ResultValueDto> resultValues = resultValueDao.findByProcessedFunctionId(processedFunctionId);
                    logger.info("Получено {} результирующих значений для обработанной функции ID: {}", resultValues.size(), processedFunctionId);
                    out.print(objectMapper.writeValueAsString(resultValues));
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

            CreateResultValueRequest createResultValueRequest = objectMapper.readValue(buffer.toString(), CreateResultValueRequest.class);

            // Проверяем, является ли пользователь владельцем связанной функции или администратором
            Optional<ProcessedFunctionDto> processedFunctionOpt = processedFunctionDao.findById(createResultValueRequest.getProcessedFunctionId());
            if (processedFunctionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Processed function not found\"}");
                return;
            }

            ProcessedFunctionDto processedFunction = processedFunctionOpt.get();
            Optional<FunctionDto> functionOpt = functionDao.findById(processedFunction.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only create result values for own functions\"}");
                return;
            }

            // Создаем ResultValueDto из CreateResultValueRequest
            ResultValueDto resultValue = new ResultValueDto(
                    createResultValueRequest.getProcessedFunctionId(),
                    createResultValueRequest.getPointIndex(),
                    createResultValueRequest.getX(),
                    createResultValueRequest.getY()
            );

            Long id = resultValueDao.save(resultValue);
            if (id != null) {
                resultValue.setId(id);
                logger.info("Создано результирующее значение с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(resultValue));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create result value\"}");
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

            // Получаем существующее результирующее значение
            Optional<ResultValueDto> existingResultValueOpt = resultValueDao.findById(id);
            if (!existingResultValueOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Result value not found\"}");
                return;
            }

            ResultValueDto existingResultValue = existingResultValueOpt.get();

            // Проверяем, является ли пользователь владельцем связанной функции или администратором
            Optional<ProcessedFunctionDto> processedFunctionOpt = processedFunctionDao.findById(existingResultValue.getProcessedFunctionId());
            if (processedFunctionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Processed function not found\"}");
                return;
            }

            ProcessedFunctionDto processedFunction = processedFunctionOpt.get();
            Optional<FunctionDto> functionOpt = functionDao.findById(processedFunction.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only update result values of own functions\"}");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            UpdateResultValueRequest updateResultValueRequest = objectMapper.readValue(buffer.toString(), UpdateResultValueRequest.class);

            // Обновляем поля
            if (updateResultValueRequest.getX() != null) {
                existingResultValue.setX(updateResultValueRequest.getX());
            }
            if (updateResultValueRequest.getY() != null) {
                existingResultValue.setY(updateResultValueRequest.getY());
            }

            boolean updated = resultValueDao.update(existingResultValue);
            if (updated) {
                logger.info("Обновлено результирующее значение с ID: {}", id);
                out.print(objectMapper.writeValueAsString(existingResultValue));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Result value not found\"}");
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

            // Проверяем, является ли пользователь владельцем связанной функции или администратором
            Optional<ResultValueDto> existingResultValueOpt = resultValueDao.findById(id);
            if (!existingResultValueOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Result value not found\"}");
                return;
            }

            ResultValueDto existingResultValue = existingResultValueOpt.get();
            Optional<ProcessedFunctionDto> processedFunctionOpt = processedFunctionDao.findById(existingResultValue.getProcessedFunctionId());
            if (processedFunctionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Processed function not found\"}");
                return;
            }

            ProcessedFunctionDto processedFunction = processedFunctionOpt.get();
            Optional<FunctionDto> functionOpt = functionDao.findById(processedFunction.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only delete result values of own functions\"}");
                return;
            }

            boolean deleted = resultValueDao.delete(id);
            if (deleted) {
                logger.info("Удалено результирующее значение с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Result value not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "DELETE");
        }
    }
}