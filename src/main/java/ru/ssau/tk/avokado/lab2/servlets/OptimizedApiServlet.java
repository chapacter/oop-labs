package ru.ssau.tk.avokado.lab2.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.dao.*;
import ru.ssau.tk.avokado.lab2.dto.OptimizedEntityDto;
import ru.ssau.tk.avokado.lab2.dto.request.SimpleEntityRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Унифицированный сервлет для работы с различными сущностями
 * Позволяет уменьшить количество специфических сервлетов
 */
@WebServlet("/api/entities/*")
public class OptimizedApiServlet extends ExceptionHandlingServlet {
    private static final Logger logger = LoggerFactory.getLogger(OptimizedApiServlet.class.getName());

    // DAO для всех сущностей
    private final UserDao userDao = new JdbcUserDao();
    private final FunctionDao functionDao = new JdbcFunctionDao();
    private final PointDao pointDao = new JdbcPointDao();
    private final OperationDao operationDao = new JdbcOperationDao();
    private final ProcessedFunctionDao processedFunctionDao = new JdbcProcessedFunctionDao();
    private final ResultValueDao resultValueDao = new JdbcResultValueDao();
    private final TabulatedFuncDao tabulatedFuncDao = new JdbcTabulatedFuncDao();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo != null ? pathInfo.split("/") : new String[]{};

            if (pathParts.length >= 2) {
                String entityName = pathParts[1];

                // Получаем список сущностей по типу
                switch (entityName.toLowerCase()) {
                    case "users":
                        handleGetUsers(request, response, out);
                        break;
                    case "functions":
                        handleGetFunctions(request, response, out);
                        break;
                    case "points":
                        handleGetPoints(request, response, out);
                        break;
                    case "operations":
                        handleGetOperations(request, response, out);
                        break;
                    case "processed-functions":
                        handleGetProcessedFunctions(request, response, out);
                        break;
                    case "result-values":
                        handleGetResultValues(request, response, out);
                        break;
                    case "tabulated-functions":
                        handleGetTabulatedFunctions(request, response, out);
                        break;
                    default:
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Unknown entity type: " + entityName + "\"}");
                        break;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Entity type is required\"}");
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

        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo != null ? pathInfo.split("/") : new String[]{};

            if (pathParts.length >= 2) {
                String entityName = pathParts[1];

                StringBuilder buffer = new StringBuilder();
                String line;
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                SimpleEntityRequest entityRequest = objectMapper.readValue(buffer.toString(), SimpleEntityRequest.class);

                switch (entityName.toLowerCase()) {
                    case "users":
                        handleCreateUser(entityRequest, response, out);
                        break;
                    case "functions":
                        handleCreateFunction(entityRequest, response, out);
                        break;
                    case "points":
                        handleCreatePoint(entityRequest, response, out);
                        break;
                    case "operations":
                        handleCreateOperation(entityRequest, response, out);
                        break;
                    case "processed-functions":
                        handleCreateProcessedFunction(entityRequest, response, out);
                        break;
                    case "result-values":
                        handleCreateResultValue(entityRequest, response, out);
                        break;
                    case "tabulated-functions":
                        handleCreateTabulatedFunction(entityRequest, response, out);
                        break;
                    default:
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Unknown entity type: " + entityName + "\"}");
                        break;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Entity type is required\"}");
            }
        } catch (Exception e) {
            handleException(e, response, logger, "POST");
        }
    }

    // Методы обработки для каждого типа сущности
    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка GET запросов для пользователей
        List<OptimizedEntityDto> users = userDao.findAll().stream()
                .map(user -> new OptimizedEntityDto(
                        OptimizedEntityDto.EntityType.USER,
                        user.getName()))
                .toList();
        out.print(objectMapper.writeValueAsString(users));
    }

    private void handleGetFunctions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка GET запросов для функций
        List<OptimizedEntityDto> functions = functionDao.findAll().stream()
                .map(func -> new OptimizedEntityDto(
                        OptimizedEntityDto.EntityType.FUNCTION,
                        func.getName()))
                .toList();
        out.print(objectMapper.writeValueAsString(functions));
    }

    private void handleGetPoints(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка GET запросов для точек
        List<OptimizedEntityDto> points = pointDao.findAll().stream()
                .map(point -> new OptimizedEntityDto(
                        OptimizedEntityDto.EntityType.POINT,
                        "Point_" + point.getId()))
                .toList();
        out.print(objectMapper.writeValueAsString(points));
    }

    private void handleGetOperations(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка GET запросов для операций
        List<OptimizedEntityDto> operations = operationDao.findAll().stream()
                .map(op -> new OptimizedEntityDto(
                        OptimizedEntityDto.EntityType.OPERATION,
                        op.getName()))
                .toList();
        out.print(objectMapper.writeValueAsString(operations));
    }

    private void handleGetProcessedFunctions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка GET запросов для обработанных функций
        List<OptimizedEntityDto> processedFunctions = processedFunctionDao.findAll().stream()
                .map(pf -> new OptimizedEntityDto(
                        OptimizedEntityDto.EntityType.PROCESSED_FUNCTION,
                        pf.getResultSummary()))
                .toList();
        out.print(objectMapper.writeValueAsString(processedFunctions));
    }

    private void handleGetResultValues(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка GET запросов для результирующих значений
        List<OptimizedEntityDto> resultValues = resultValueDao.findAll().stream()
                .map(rv -> new OptimizedEntityDto(
                        OptimizedEntityDto.EntityType.RESULT_VALUE,
                        "ResultValue_" + rv.getId()))
                .toList();
        out.print(objectMapper.writeValueAsString(resultValues));
    }

    private void handleGetTabulatedFunctions(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка GET запросов для табулированных функций
        List<OptimizedEntityDto> tabulatedFunctions = tabulatedFuncDao.findAll().stream()
                .map(tf -> new OptimizedEntityDto(
                        OptimizedEntityDto.EntityType.TABULATED_FUNCTION,
                        "TabulatedFunction_" + tf.getId()))
                .toList();
        out.print(objectMapper.writeValueAsString(tabulatedFunctions));
    }

    private void handleCreateUser(SimpleEntityRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка POST запросов для создания пользователей
        OptimizedEntityDto newUser = new OptimizedEntityDto(OptimizedEntityDto.EntityType.USER, request.getName());
        newUser.setValue(request.getValue());
        newUser.setIntValue(request.getIntValue());

        // Возвращаем успешный ответ
        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(objectMapper.writeValueAsString(newUser));
    }

    private void handleCreateFunction(SimpleEntityRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка POST запросов для создания функций
        OptimizedEntityDto newFunction = new OptimizedEntityDto(OptimizedEntityDto.EntityType.FUNCTION, request.getName());
        newFunction.setReferenceId1(request.getReferenceId1()); // userId
        newFunction.setNumericValue1(request.getXVal()); // format
        newFunction.setValue(request.getValue()); // funcResult

        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(objectMapper.writeValueAsString(newFunction));
    }

    private void handleCreatePoint(SimpleEntityRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка POST запросов для создания точек
        OptimizedEntityDto newPoint = new OptimizedEntityDto(OptimizedEntityDto.EntityType.POINT, request.getName());
        newPoint.setReferenceId1(request.getReferenceId1()); // functionId
        newPoint.setNumericValue1(request.getXVal()); // x
        newPoint.setNumericValue2(request.getYVal()); // y
        newPoint.setIntValue(request.getIntValue()); // pointIndex

        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(objectMapper.writeValueAsString(newPoint));
    }

    private void handleCreateOperation(SimpleEntityRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка POST запросов для создания операций
        OptimizedEntityDto newOperation = new OptimizedEntityDto(OptimizedEntityDto.EntityType.OPERATION, request.getName());
        newOperation.setValue(request.getValue()); // description

        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(objectMapper.writeValueAsString(newOperation));
    }

    private void handleCreateProcessedFunction(SimpleEntityRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка POST запросов для создания обработанных функций
        OptimizedEntityDto newProcessedFunction = new OptimizedEntityDto(
                OptimizedEntityDto.EntityType.PROCESSED_FUNCTION, request.getName());
        newProcessedFunction.setReferenceId1(request.getReferenceId1()); // functionId
        newProcessedFunction.setReferenceId2(request.getReferenceId2()); // operationId
        newProcessedFunction.setValue(request.getValue()); // resultSummary

        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(objectMapper.writeValueAsString(newProcessedFunction));
    }

    private void handleCreateResultValue(SimpleEntityRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка POST запросов для создания результирующих значений
        OptimizedEntityDto newResultValue = new OptimizedEntityDto(
                OptimizedEntityDto.EntityType.RESULT_VALUE, "ResultValue");
        newResultValue.setReferenceId1(request.getReferenceId1()); // processedFunctionId
        newResultValue.setIntValue(request.getIntValue()); // pointIndex
        newResultValue.setNumericValue1(request.getXVal()); // x
        newResultValue.setNumericValue2(request.getYVal()); // y

        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(objectMapper.writeValueAsString(newResultValue));
    }

    private void handleCreateTabulatedFunction(SimpleEntityRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        // Обработка POST запросов для создания табулированных функций
        OptimizedEntityDto newTabulatedFunction = new OptimizedEntityDto(
                OptimizedEntityDto.EntityType.TABULATED_FUNCTION, "TabulatedFunction");
        newTabulatedFunction.setReferenceId1(request.getReferenceId1()); // funcId
        newTabulatedFunction.setNumericValue1(request.getXVal()); // xVal
        newTabulatedFunction.setNumericValue2(request.getYVal()); // yVal

        response.setStatus(HttpServletResponse.SC_CREATED);
        out.print(objectMapper.writeValueAsString(newTabulatedFunction));
    }
}