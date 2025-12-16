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
import ru.ssau.tk.avokado.lab2.dao.JdbcTabulatedFuncDao;
import ru.ssau.tk.avokado.lab2.dao.TabulatedFuncDao;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.TabulatedFuncDto;
import ru.ssau.tk.avokado.lab2.dto.UserDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/tabulated-functions/*")
public class TabulatedFunctionServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionServlet.class.getName());
    private final TabulatedFuncDao tabulatedFuncDao = new JdbcTabulatedFuncDao();
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
                // GET /api/tabulated-functions - получить все табулированные функции
                // Обычные пользователи могут видеть только табулированные функции своих функций, администраторы - все
                List<TabulatedFuncDto> tabulatedFuncs;
                if (authenticatedUser.getRole() == Role.ADMIN) {
                    tabulatedFuncs = tabulatedFuncDao.findAll();
                } else {
                    // Получаем все функции пользователя, а затем табулированные функции для этих функций
                    List<FunctionDto> userFunctions = functionDao.findByUserId(authenticatedUser.getId());
                    tabulatedFuncs = new java.util.ArrayList<>();
                    for (FunctionDto function : userFunctions) {
                        tabulatedFuncs.addAll(tabulatedFuncDao.findByFuncId(function.getId()));
                    }
                }
                logger.info("Получено {} табулированных функций", tabulatedFuncs.size());
                out.print(objectMapper.writeValueAsString(tabulatedFuncs));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    // GET /api/tabulated-functions/{id}
                    Long id = Long.parseLong(pathParts[1]);
                    Optional<TabulatedFuncDto> tabulatedFunc = tabulatedFuncDao.findById(id);
                    if (tabulatedFunc.isPresent()) {
                        TabulatedFuncDto tf = tabulatedFunc.get();
                        // Проверяем, является ли пользователь владельцем связанной функции или администратором
                        Optional<FunctionDto> functionOpt = functionDao.findById(tf.getFuncId());
                        if (functionOpt.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"error\":\"Function not found\"}");
                            return;
                        }

                        FunctionDto function = functionOpt.get();
                        if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            out.print("{\"error\":\"Access denied: can only access tabulated functions of own functions\"}");
                            return;
                        }

                        logger.info("Получена табулированная функция с ID: {}", id);
                        out.print(objectMapper.writeValueAsString(tabulatedFunc.get()));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Tabulated function not found\"}");
                    }
                } else if (pathParts.length == 3 && pathParts[1].equals("func")) {
                    // GET /api/tabulated-functions/func/{funcId}
                    Long funcId = Long.parseLong(pathParts[2]);
                    // Проверяем, является ли пользователь владельцем функции или администратором
                    Optional<FunctionDto> functionOpt = functionDao.findById(funcId);
                    if (functionOpt.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Function not found\"}");
                        return;
                    }

                    FunctionDto function = functionOpt.get();
                    if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\":\"Access denied: can only access tabulated functions of own functions\"}");
                        return;
                    }

                    List<TabulatedFuncDto> tabulatedFuncs = tabulatedFuncDao.findByFuncId(funcId);
                    logger.info("Получено {} табулированных функций для функции ID: {}", tabulatedFuncs.size(), funcId);
                    out.print(objectMapper.writeValueAsString(tabulatedFuncs));
                } else if (pathParts.length == 3 && pathParts[1].equals("x")) {
                    // GET /api/tabulated-functions/x/{xVal}
                    Double xVal = Double.parseDouble(pathParts[2]);
                    // Для поиска по значению x, нужно получить все табулированные функции с этим значением,
                    // а затем отфильтровать по принадлежности пользователю
                    List<TabulatedFuncDto> allTabulatedFuncs = tabulatedFuncDao.findByXVal(xVal);
                    List<TabulatedFuncDto> userTabulatedFuncs = new java.util.ArrayList<>();

                    if (authenticatedUser.getRole() == Role.ADMIN) {
                        userTabulatedFuncs = allTabulatedFuncs;
                    } else {
                        for (TabulatedFuncDto tf : allTabulatedFuncs) {
                            Optional<FunctionDto> functionOpt = functionDao.findById(tf.getFuncId());
                            if (functionOpt.isPresent() && functionOpt.get().getUserId().equals(authenticatedUser.getId())) {
                                userTabulatedFuncs.add(tf);
                            }
                        }
                    }

                    logger.info("Получено {} табулированных функций с x значением: {}", userTabulatedFuncs.size(), xVal);
                    out.print(objectMapper.writeValueAsString(userTabulatedFuncs));
                } else if (pathParts.length == 3 && pathParts[1].equals("y")) {
                    // GET /api/tabulated-functions/y/{yVal}
                    Double yVal = Double.parseDouble(pathParts[2]);
                    // Для поиска по значению y, нужно получить все табулированные функции с этим значением,
                    // а затем отфильтровать по принадлежности пользователю
                    List<TabulatedFuncDto> allTabulatedFuncs = tabulatedFuncDao.findByYVal(yVal);
                    List<TabulatedFuncDto> userTabulatedFuncs = new java.util.ArrayList<>();

                    if (authenticatedUser.getRole() == Role.ADMIN) {
                        userTabulatedFuncs = allTabulatedFuncs;
                    } else {
                        for (TabulatedFuncDto tf : allTabulatedFuncs) {
                            Optional<FunctionDto> functionOpt = functionDao.findById(tf.getFuncId());
                            if (functionOpt.isPresent() && functionOpt.get().getUserId().equals(authenticatedUser.getId())) {
                                userTabulatedFuncs.add(tf);
                            }
                        }
                    }

                    logger.info("Получено {} табулированных функций с y значением: {}", userTabulatedFuncs.size(), yVal);
                    out.print(objectMapper.writeValueAsString(userTabulatedFuncs));
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

            TabulatedFuncDto tabulatedFunc = objectMapper.readValue(buffer.toString(), TabulatedFuncDto.class);

            // Проверяем, является ли пользователь владельцем связанной функции или администратором
            Optional<FunctionDto> functionOpt = functionDao.findById(tabulatedFunc.getFuncId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only create tabulated functions for own functions\"}");
                return;
            }

            Long id = tabulatedFuncDao.save(tabulatedFunc);
            if (id != null) {
                tabulatedFunc.setId(id);
                logger.info("Создана табулированная функция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(objectMapper.writeValueAsString(tabulatedFunc));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create tabulated function\"}");
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

            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            TabulatedFuncDto tabulatedFunc = objectMapper.readValue(buffer.toString(), TabulatedFuncDto.class);
            tabulatedFunc.setId(id);

            // Проверяем, является ли пользователь владельцем связанной функции или администратором
            Optional<FunctionDto> functionOpt = functionDao.findById(tabulatedFunc.getFuncId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only update tabulated functions of own functions\"}");
                return;
            }

            boolean updated = tabulatedFuncDao.update(tabulatedFunc);
            if (updated) {
                logger.info("Обновлена табулированная функция с ID: {}", id);
                out.print(objectMapper.writeValueAsString(tabulatedFunc));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Tabulated function not found\"}");
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
            Optional<TabulatedFuncDto> existingTabulatedFuncOpt = tabulatedFuncDao.findById(id);
            if (!existingTabulatedFuncOpt.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Tabulated function not found\"}");
                return;
            }

            TabulatedFuncDto existingTabulatedFunc = existingTabulatedFuncOpt.get();
            Optional<FunctionDto> functionOpt = functionDao.findById(existingTabulatedFunc.getFuncId());
            if (functionOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Function not found\"}");
                return;
            }

            FunctionDto function = functionOpt.get();
            if (!authenticatedUser.getRole().equals(Role.ADMIN) && !function.getUserId().equals(authenticatedUser.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"Access denied: can only delete tabulated functions of own functions\"}");
                return;
            }

            boolean deleted = tabulatedFuncDao.delete(id);
            if (deleted) {
                logger.info("Удалена табулированная функция с ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Tabulated function not found\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "DELETE");
        }
    }
}