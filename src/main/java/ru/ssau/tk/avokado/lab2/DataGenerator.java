package ru.ssau.tk.avokado.lab2;

import ru.ssau.tk.avokado.lab2.dao.*;
import ru.ssau.tk.avokado.lab2.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {
    private static final Random random = new Random();

    // Генерация точек для функции
    public static List<PointDto> generatePointsDto(Long functionId, int count, double xFrom, double xTo) {
        List<PointDto> points = new ArrayList<>();
        double step = (xTo - xFrom) / (count - 1);

        for (int i = 0; i < count; i++) {
            double x = xFrom + i * step;
            double y = Math.sin(x) + random.nextDouble() * 0.1; // Небольшой шум
            points.add(new PointDto(functionId, x, y, i));
        }

        return points;
    }

    // Генерация точек для табулированной функции
    public static List<TabulatedFuncDto> generateTabulatedFuncDto(Long funcId, int count, double xFrom, double xTo) {
        List<TabulatedFuncDto> tbFunc = new ArrayList<>();
        double step = (xTo - xFrom) / (count - 1);

        for (int i = 0; i < count; i++) {
            double x = xFrom + i * step;
            double y = Math.sin(x) + random.nextDouble() * 0.1; // Небольшой шум
            tbFunc.add(new TabulatedFuncDto(funcId, x, y));
        }

        return tbFunc;
    }

    // Генерация функций
    public static List<FunctionDto> generateFunctionsDto(Long userId, int count) {
        List<FunctionDto> functions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            functions.add(new FunctionDto(
                    userId,
                    "TestFunction_" + i,
                    i % 2,
                    "Generated function result " + i
            ));
        }

        return functions;
    }

    // Генерация операций
    public static List<OperationDto> generateOperationsDto(int count) {
        List<OperationDto> operations = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            operations.add(new OperationDto(
                    "Operation_" + i,
                    "Description for operation " + i
            ));
        }

        return operations;
    }

    // Генерация обработанных функций
    public static List<ProcessedFunctionDto> generateProcessedFunctionsDto(Long functionId, Long operationId, int count) {
        List<ProcessedFunctionDto> processedFunctions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            processedFunctions.add(new ProcessedFunctionDto(
                    functionId,
                    operationId,
                    "Result summary for processed function " + i
            ));
        }

        return processedFunctions;
    }

    // Генерация значений результатов
    public static List<ResultValueDto> generateResultValuesDto(Long processedFunctionId, int count, double xFrom, double xTo) {
        List<ResultValueDto> resultValues = new ArrayList<>();
        double step = (xTo - xFrom) / (count - 1);

        for (int i = 0; i < count; i++) {
            double x = xFrom + i * step;
            double y = Math.cos(x) + random.nextDouble() * 0.1; // Небольшой шум
            resultValues.add(new ResultValueDto(processedFunctionId, i, x, y));
        }

        return resultValues;
    }

    // Генерация пользователей
    public static List<String> generateUsernames(int count) {
        List<String> usernames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            usernames.add("generated_user_" + System.currentTimeMillis() + "_" + i);
        }
        return usernames;
    }

    static void main(String[] args) {
        try {
            // Создаем DAO объекты
            UserDao userDao = new JdbcUserDao();
            FunctionDao functionDao = new JdbcFunctionDao();
            PointDao pointDao = new JdbcPointDao();
            OperationDao operationDao = new JdbcOperationDao();
            ProcessedFunctionDao processedFunctionDao = new JdbcProcessedFunctionDao();
            ResultValueDao resultValueDao = new JdbcResultValueDao();
            TabulatedFuncDao tabulatedFuncDao = new JdbcTabulatedFuncDao();

            // Генерируем и сохраняем пользователей
            List<String> usernames = DataGenerator.generateUsernames(3);
            for (String username : usernames) {
                UserDto user = new UserDto(username, "hashed_password", Role.USER);
                Long userId = userDao.save(user);
                System.out.println("Создан пользователь с ID: " + userId);

                // Генерируем и сохраняем функции для пользователя
                List<FunctionDto> functions = DataGenerator.generateFunctionsDto(userId, 2);
                for (FunctionDto function : functions) {
                    Long functionId = functionDao.save(function);
                    System.out.println("Создана функция с ID: " + functionId);

                    // Генерируем и сохраняем точки для функции
                    List<PointDto> points = DataGenerator.generatePointsDto(functionId, 5, 0.0, 10.0);
                    functionDao.savePoints(functionId, points);
                    System.out.println("Создано " + points.size() + " точек для функции " + functionId);

                    // Генерируем и сохраняем табулированные функции
                    List<TabulatedFuncDto> tabulatedFuncs = DataGenerator.generateTabulatedFuncDto(functionId, 5, 0.0, 10.0);
                    for (TabulatedFuncDto tabulatedFunc : tabulatedFuncs) {
                        Long tabulatedFuncId = tabulatedFuncDao.save(tabulatedFunc);
                        System.out.println("Создана табулированная функция с ID: " + tabulatedFuncId);
                    }
                }
            }

            // Генерируем и сохраняем операции
            List<OperationDto> operations = DataGenerator.generateOperationsDto(3);
            for (OperationDto operation : operations) {
                Long operationId = operationDao.save(operation);
                System.out.println("Создана операция с ID: " + operationId);
            }

            // Создаем обработанные функции и значения результатов (по 1-му пользователю)
            List<UserDto> allUsers = userDao.findAll();
            if (!allUsers.isEmpty()) {
                UserDto user = allUsers.get(0);
                List<FunctionDto> userFunctions = functionDao.findByUserId(user.getId());
                if (!userFunctions.isEmpty()) {
                    FunctionDto function = userFunctions.get(0);
                    List<OperationDto> allOperations = operationDao.findAll();
                    if (!allOperations.isEmpty()) {
                        OperationDto operation = allOperations.get(0);

                        // Генерируем и сохраняем обработанные функции
                        List<ProcessedFunctionDto> processedFunctions = DataGenerator.generateProcessedFunctionsDto(
                                function.getId(), operation.getId(), 2);
                        for (ProcessedFunctionDto processedFunction : processedFunctions) {
                            Long processedFunctionId = processedFunctionDao.save(processedFunction);
                            System.out.println("Создана обработанная функция с ID: " + processedFunctionId);

                            // Генерируем и сохраняем значения результатов
                            List<ResultValueDto> resultValues = DataGenerator.generateResultValuesDto(
                                    processedFunctionId, 3, 0.0, 5.0);
                            for (ResultValueDto resultValue : resultValues) {
                                Long resultValueId = resultValueDao.save(resultValue);
                                System.out.println("Создано значение результата с ID: " + resultValueId);
                            }
                        }
                    }
                }
            }

            System.out.println("Генерация данных завершена!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}