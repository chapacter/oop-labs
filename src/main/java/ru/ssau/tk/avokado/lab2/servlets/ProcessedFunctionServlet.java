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
import ru.ssau.tk.avokado.lab2.dao.JdbcProcessedFunctionDao;
import ru.ssau.tk.avokado.lab2.dao.ProcessedFunctionDao;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.ProcessedFunctionDto;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.dto.request.CreateProcessedFunctionRequest;
import ru.ssau.tk.avokado.lab2.dto.request.UpdateProcessedFunctionRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/processed-functions/*")
public class ProcessedFunctionServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProcessedFunctionServlet.class.getName());
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
                // GET /api/processed-functions - получить все обработанные функции
                // Обычные пользователи могут видеть только обработанные функции своих функций, администраторы - все
                List<ProcessedFunctionDto> processedFunctions;
                if (authenticatedUser.getRole() == Role.ADMIN) {
                    processedFunctions = processedFunctionDao.findAll();
                } else {
                    // Получаем все функции пользователя, а затем обработанные функции для этих функций
                    List<FunctionDto> userFunctions = functionDao.findByUserId(authenticatedUser.getId());
                    processedFunctions = new java.util.ArrayList<>();
                    for (FunctionDto function : userFunctions) {
                        processedFunctions.addAll(processedFunctionDao.findByFunctionId(function.getId()));
                    }
                }
                logger.info("Получено {} обработанных функций", processedFunctions.size());
                out.print(objectMapper.writeValueAsString(processedFunctions));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    // GET /api/processed-functions/{id}
                    Long id = Long.parseLong(pathParts[1]);
                    Optional<ProcessedFunctionDto> processedFunction = processedFunctionDao.findById(id);
                    if (processedFunction.isPresent()) {
                        ProcessedFunctionDto pf = processedFunction.get();
                        // Проверяем, является ли пользователь владельцем связанной функции или администратором
                        Optional<FunctionDto> functionOpt = functionDao.findById(pf.getFunctionId());
                        if (functionOpt.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"error\":\"Function not found\"}");
                            return;
                        }

                        FunctionDto function = functionOpt.get();
                        if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            out.print("{\"error\":\"Access denied: can only access processed functions of own functions\"}");
                            return;
                        }

                        logger.info("Получена обработанная функция с ID: {}", id);
                        out.print(objectMapper.writeValueAsString(processedFunction.get()));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Processed function not found\"}");
                    }
                } else if (pathParts.length == 3 && pathParts[1].equals("function")) {
                    // GET /api/processed-functions/function/{functionId}
                    Long functionId = Long.parseLong(pathParts[2]);
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
                        out.print("{\"error\":\"Access denied: can only access processed functions of own functions\"}");
                        return;
                    }

                    List<ProcessedFunctionDto> processedFunctions = processedFunctionDao.findByFunctionId(functionId);
                    logger.info("Получено {} обработанных функций для функции ID: {}", processedFunctions.size(), functionId);
                    out.print(objectMapper.writeValueAsString(processedFunctions));
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

            CreateProcessedFunctionRequest createProcessedFunctionRequest = objectMapper.readValue(buffer.toString(), CreateProcessedFunctionRequest.class);

            // Проверяем, является ли пользователь владельцем связанной функции или администратором
            Optional<FunctionDto> functionOpt = functionDao.findById(createProcessedFunctionRequest.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only create processed functions for own functions\"}");
                return;
            }

            // Создаем ProcessedFunctionDto из CreateProcessedFunctionRequest
            ProcessedFunctionDto processedFunction = new ProcessedFunctionDto(
                    createProcessedFunctionRequest.getFunctionId(),
                    createProcessedFunctionRequest.getOperationId(),
                    createProcessedFunctionRequest.getResultSummary()
            );

            Long id = processedFunctionDao.save(processedFunction);
            if (id != null) {
                processedFunction.setId(id);
                logger.info("Создана обработанная функция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(processedFunction));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create processed function\"}");
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

            // Получаем существующую обработанную функцию
            Optional<ProcessedFunctionDto> existingProcessedFunctionOpt = processedFunctionDao.findById(id);
            if (!existingProcessedFunctionOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Processed function not found\"}");
                return;
            }

            ProcessedFunctionDto existingProcessedFunction = existingProcessedFunctionOpt.get();

            // Проверяем, является ли пользователь владельцем связанной функции или администратором
            Optional<FunctionDto> functionOpt = functionDao.findById(existingProcessedFunction.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only update processed functions of own functions\"}");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            UpdateProcessedFunctionRequest updateProcessedFunctionRequest = objectMapper.readValue(buffer.toString(), UpdateProcessedFunctionRequest.class);

            // Обновляем поля
            if (updateProcessedFunctionRequest.getResultSummary() != null) {
                existingProcessedFunction.setResultSummary(updateProcessedFunctionRequest.getResultSummary());
            }

            boolean updated = processedFunctionDao.update(existingProcessedFunction);
            if (updated) {
                logger.info("Обновлена обработанная функция с ID: {}", id);
                out.print(objectMapper.writeValueAsString(existingProcessedFunction));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Processed function not found\"}");
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
            Optional<ProcessedFunctionDto> existingProcessedFunctionOpt = processedFunctionDao.findById(id);
            if (!existingProcessedFunctionOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Processed function not found\"}");
                return;
            }

            ProcessedFunctionDto existingProcessedFunction = existingProcessedFunctionOpt.get();
            Optional<FunctionDto> functionOpt = functionDao.findById(existingProcessedFunction.getFunctionId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only delete processed functions of own functions\"}");
                return;
            }

            boolean deleted = processedFunctionDao.delete(id);
            if (deleted) {
                logger.info("Удалена обработанная функция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Processed function not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "DELETE");
        }
    }
}