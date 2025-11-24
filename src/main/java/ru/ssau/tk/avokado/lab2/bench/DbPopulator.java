package ru.ssau.tk.avokado.lab2.bench;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record DbPopulator(Connection connection) {

    public DbPopulator(Connection connection) {
        this.connection = connection;
    }

    public boolean isPopulated(int requiredFunctions) throws SQLException {
        String sql = "SELECT COUNT(*) FROM functions";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long cnt = rs.getLong(1);
                return cnt >= requiredFunctions;
            }
        }
        return false;
    }

    public void populate(int totalFunctions, int pointsPerFunction, int batchSize) throws SQLException {
        System.out.println("Clearing and populating " + totalFunctions + " functions (and points) via JDBC...");

        // Очистка таблиц
        try (PreparedStatement stmt1 = connection.prepareStatement("DELETE FROM points");
             PreparedStatement stmt2 = connection.prepareStatement("DELETE FROM functions");
             PreparedStatement stmt3 = connection.prepareStatement("DELETE FROM users")) {
            stmt1.executeUpdate();
            stmt2.executeUpdate();
            stmt3.executeUpdate();
        }

        // Создание seed пользователя
        long userId;
        String insertUserSql = "INSERT INTO users (name, password_hash, access_lvl) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement userStmt = connection.prepareStatement(insertUserSql)) {
            userStmt.setString(1, "seed_user");
            userStmt.setString(2, "pass");
            userStmt.setInt(3, 1);
            ResultSet userRs = userStmt.executeQuery();
            userRs.next();
            userId = userRs.getLong(1);
        }

        // Вставка функций партиями
        String insertFunctionSql = "INSERT INTO functions (name, user_id, func_result) VALUES (?, ?, ?) RETURNING id";
        List<Long> functionIds = new ArrayList<>();

        int created = 0;
        while (created < totalFunctions) {
            List<Long> batchIds = new ArrayList<>();
            int chunk = Math.min(batchSize, totalFunctions - created);

            for (int i = 0; i < chunk; i++) {
                try (PreparedStatement funcStmt = connection.prepareStatement(insertFunctionSql)) {
                    funcStmt.setString(1, "func_" + (created + i));
                    funcStmt.setLong(2, userId);
                    funcStmt.setString(3, "result_" + (created + i));
                    ResultSet funcRs = funcStmt.executeQuery();
                    funcRs.next();
                    batchIds.add(funcRs.getLong(1));
                }
            }
            functionIds.addAll(batchIds);
            created += chunk;
            if (created % 1000 == 0) System.out.println("Inserted " + created + " / " + totalFunctions);
        }

        // Вставка точек
        String insertPointSql = "INSERT INTO points (x, y, index_in_function, function_id) VALUES (?, ?, ?, ?)";
        List<Object[]> pBatch = new ArrayList<>(batchSize * 2);

        for (int i = 0; i < functionIds.size(); i++) {
            long funcId = functionIds.get(i);
            for (int j = 0; j < pointsPerFunction; j++) {
                Object[] pointData = new Object[4];
                pointData[0] = i + j * 0.1; // x
                pointData[1] = Math.sin(i + j); // y
                pointData[2] = j; // index
                pointData[3] = funcId; // function_id
                pBatch.add(pointData);

                if (pBatch.size() >= batchSize) {
                    executeBatchInsertPoints(pBatch);
                    pBatch.clear();
                }
            }
        }

        if (!pBatch.isEmpty()) {
            executeBatchInsertPoints(pBatch);
        }

        System.out.println("Populate finished. functions=" + countFunctions() + ", points=" + countPoints());
    }

    private void executeBatchInsertPoints(List<Object[]> batch) throws SQLException {
        String insertPointSql = "INSERT INTO points (x, y, index_in_function, function_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pointStmt = connection.prepareStatement(insertPointSql)) {
            for (Object[] pointData : batch) {
                pointStmt.setDouble(1, (Double) pointData[0]);
                pointStmt.setDouble(2, (Double) pointData[1]);
                pointStmt.setInt(3, (Integer) pointData[2]);
                pointStmt.setLong(4, (Long) pointData[3]);
                pointStmt.addBatch();
            }
            pointStmt.executeBatch();
        }
    }

    private long countFunctions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM functions";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getLong(1);
        }
    }

    private long countPoints() throws SQLException {
        String sql = "SELECT COUNT(*) FROM points";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getLong(1);
        }
    }
}