package ru.ssau.tk.avokado.lab2.bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BenchmarkService {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkService.class);

    private final Connection connection;
    private final DbPopulator populator;

    public BenchmarkService(Connection connection) {
        this.connection = connection;
        this.populator = new DbPopulator(connection);
    }

    public BenchmarkService(Connection connection, Connection connection1, DbPopulator populator) {
        this.connection = connection1;
        this.populator = populator;
    }

    public void run() throws Exception {
        int totalFunctions = 20000;
        int batchSize = 20000;
        int warmup = 200;
        int measured = 20000;

        if (!populator.isPopulated(totalFunctions)) {
            populator.populate(totalFunctions, 2, batchSize);
        } else {
            logger.info("DB already has >= {} functions (JDBC).", totalFunctions);
        }

        // 1) find user by name (JDBC)
        runJdbcBench("findUserByName_jdbc", warmup, measured, this::opFindUserByName);

        // 2) find function by name (JDBC)
        runJdbcBench("findFunctionByName_jdbc", warmup, measured, this::opFindFunctionByName);

        // 3) load function and touch points collection (join-like)
        runJdbcBench("loadFunctionAndPoints_jdbc", warmup, measured, this::opLoadFunctionAndPoints);

        logger.info("JDBC-only benchmarks finished. CSV files in project root.");
    }

    /**
     * Универсальный раннер: прогрев + измерения + запись CSV + лог с медианой.
     */
    private void runJdbcBench(String testName, int warmup, int measured, Action action) {
        logger.info("Benchmarking {} (warmup={} measured={})", testName, warmup, measured);

        for (int i = 0; i < warmup; i++) {
            try {
                action.run();
            } catch (Exception ex) { /* ignore warmup errors */ }
        }

        List<Long> times = new ArrayList<>(measured);
        for (int i = 0; i < measured; i++) {
            long s = System.nanoTime();
            try {
                action.run();
            } catch (Exception ex) {
                logger.warn("Iteration {} thrown exception in {}: {}", i, testName, ex.toString());
            }
            long e = System.nanoTime();
            times.add(e - s);
        }

        String csv = testName + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(csv))) {
            pw.println("test,impl,iteration,nanos");
            for (int i = 0; i < times.size(); i++) {
                pw.println(testName + ",jdbc," + i + "," + times.get(i));
            }
            pw.flush();
        } catch (Exception ex) {
            logger.error("Failed to write CSV {}: {}", csv, ex.toString());
        }

        long med = median(times);
        logger.info("Wrote {} (median ~ {} ns)", csv, med);
    }

    private long median(List<Long> arr) {
        if (arr == null || arr.isEmpty()) return 0L;
        List<Long> copy = new ArrayList<>(arr);
        Collections.sort(copy);
        int n = copy.size();
        if (n % 2 == 1) {
            return copy.get(n / 2);
        } else {
            return (copy.get(n / 2 - 1) + copy.get(n / 2)) / 2;
        }
    }

    void opFindUserByName() throws SQLException {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "seed_user");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rs.getLong("id");
            }
        }
    }

    void opFindFunctionByName() throws SQLException {
        String sql = "SELECT * FROM functions WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "func_0");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Получаем результат, чтобы убедиться, что запрос завершен
                rs.getLong("id");
            }
        }
    }

    void opLoadFunctionAndPoints() throws SQLException {
        String sql = "SELECT f.id FROM functions f WHERE f.name = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "func_0");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long funcId = rs.getLong("id");

                String pointsSql = "SELECT * FROM points WHERE function_id = ?";
                try (PreparedStatement pointsStmt = connection.prepareStatement(pointsSql)) {
                    pointsStmt.setLong(1, funcId);
                    ResultSet pointsRs = pointsStmt.executeQuery();
                    int count = 0;
                    while (pointsRs.next()) {
                        count++;
                    }
                }
            }
        }
    }

    private interface Action {
        void run() throws Exception;
    }
}