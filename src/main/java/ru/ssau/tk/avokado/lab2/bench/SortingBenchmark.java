package ru.ssau.tk.avokado.lab2.bench;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SortingBenchmark {

    private final Connection connection;
    private final DbPopulator populator;

    public SortingBenchmark(Connection connection) {
        this.connection = connection;
        this.populator = new DbPopulator(connection);
    }

    public void run() {
        try {
            runBench();
        } catch (Throwable t) {
            System.err.println("SortingBenchmarkJDBC failed: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void runBench() throws Exception {
        int totalFunctions = 10_000;
        int pointsPerFunction = 2;
        int batchSize = 500;
        int warmup = 20;
        int measured = 100;

        if (!populator.isPopulated(totalFunctions)) {
            populator.populate(totalFunctions, pointsPerFunction, batchSize);
        } else {
            System.out.println("DB already populated.");
        }

        System.out.println("Starting extended sorting benchmarks (JDBC)...");

        runBoth("sort_users_by_name_asc",
                () -> fetchUsersWithSort("ORDER BY name ASC"),
                () -> fetchAllUsersAndSort(Comparator.comparing(User::getName, Comparator.nullsFirst(String::compareTo))),
                warmup, measured);

        runBoth("sort_users_by_name_desc",
                () -> fetchUsersWithSort("ORDER BY name DESC"),
                () -> fetchAllUsersAndSort(Comparator.comparing(User::getName, Comparator.nullsFirst(String::compareTo)).reversed()),
                warmup, measured);

        runBoth("sort_users_by_id_asc",
                () -> fetchUsersWithSort("ORDER BY id ASC"),
                () -> fetchAllUsersAndSort(Comparator.comparing(User::getId, Comparator.nullsFirst(Long::compareTo))),
                warmup, measured);

        runBoth("sort_functions_by_name_asc",
                () -> fetchFunctionsWithSort("ORDER BY name ASC"),
                () -> fetchAllFunctionsAndSort(Comparator.comparing(Function::getName, Comparator.nullsFirst(String::compareTo))),
                warmup, measured);

        runBoth("sort_functions_by_user_id_asc",
                () -> fetchFunctionsWithSort("ORDER BY user_id ASC"),
                () -> fetchAllFunctionsAndSort(Comparator.comparing(f -> f.userId != 0 ? f.userId : -1L)),
                warmup, measured);

        runBoth("sort_points_by_x_asc",
                () -> fetchPointsWithSort("ORDER BY x ASC"),
                () -> fetchAllPointsAndSort(Comparator.comparingDouble(Point::getX)),
                warmup, measured);

        runBoth("sort_points_by_function_id_asc",
                () -> fetchPointsWithSort("ORDER BY function_id ASC"),
                () -> fetchAllPointsAndSort(Comparator.comparing(p -> p.functionId != 0 ? p.functionId : -1L)),
                warmup, measured);

        System.out.println("All sorting benchmarks finished. CSVs and aggregates written to project root.");
    }

    private <T> void runBoth(String name, DbFetch<T> dbFetch, MemFetch<T> memFetch, int warmup, int measured) throws Exception {
        System.out.println("Benchmarking " + name + " (warmup=" + warmup + " measured=" + measured + ")");

        // DB
        for (int i = 0; i < warmup; i++) dbFetch.get();
        List<Long> dbTimes = new ArrayList<>(measured);
        for (int i = 0; i < measured; i++) {
            long s = System.nanoTime();
            List<T> res = dbFetch.get();
            int sz = res.size();
            long e = System.nanoTime();
            dbTimes.add(e - s);
        }
        writeCsv("sort_" + name + "_db.csv", "test,impl,iteration,nanos", name + ",db,", dbTimes);

        // Memory
        for (int i = 0; i < warmup; i++) memFetch.get();
        List<Long> memTimes = new ArrayList<>(measured);
        for (int i = 0; i < measured; i++) {
            long s = System.nanoTime();
            List<T> res = memFetch.get();
            int sz = res.size();
            long e = System.nanoTime();
            memTimes.add(e - s);
        }
        writeCsv("sort_" + name + "_memory.csv", "test,impl,iteration,nanos", name + ",memory,", memTimes);

        Map<String, Long> aggDb = aggregates(dbTimes);
        Map<String, Long> aggMem = aggregates(memTimes);

        synchronized (SortingBenchmark.class) {
            boolean exists = new java.io.File("aggregates_sorting.csv").exists();
            try (PrintWriter pw = new PrintWriter(new FileWriter("aggregates_sorting.csv", true))) {
                if (!exists) pw.println("test,impl,n,mean_ns,median_ns,std_ns");
                pw.println(String.format("%s,db,%d,%d,%d,%d", name, dbTimes.size(), aggDb.get("mean"), aggDb.get("median"), aggDb.get("std")));
                pw.println(String.format("%s,memory,%d,%d,%d,%d", name, memTimes.size(), aggMem.get("mean"), aggMem.get("median"), aggMem.get("std")));
            }
        }

        System.out.println("Wrote sort_" + name + "_db.csv and sort_" + name + "_memory.csv (median db " + aggDb.get("median") + " ns, mem " + aggMem.get("median") + " ns)");
    }

    private void writeCsv(String fname, String header, String prefix, List<Long> times) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fname))) {
            pw.println(header);
            for (int i = 0; i < times.size(); i++) {
                pw.println(prefix + i + "," + times.get(i));
            }
        }
    }

    private Map<String, Long> aggregates(List<Long> arr) {
        Map<String, Long> out = new HashMap<>();
        if (arr == null || arr.isEmpty()) {
            out.put("mean", 0L);
            out.put("median", 0L);
            out.put("std", 0L);
            return out;
        }
        int n = arr.size();
        double sum = 0;
        for (long v : arr) sum += v;
        long mean = Math.round(sum / n);
        List<Long> sorted = arr.stream().sorted().collect(Collectors.toList());
        long median = sorted.get(n / 2);
        double ssd = 0;
        for (long v : arr) {
            double d = v - mean;
            ssd += d * d;
        }
        long std = Math.round(Math.sqrt(ssd / n));
        out.put("mean", mean);
        out.put("median", median);
        out.put("std", std);
        return out;
    }

    // Методы для получения данных с сортировкой из БД
    private List<User> fetchUsersWithSort(String orderBy) throws SQLException {
        String sql = "SELECT id, name, password_hash, access_lvl FROM users " + orderBy;
        List<User> users = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.id = rs.getLong("id");
                user.name = rs.getString("name");
                user.passwordHash = rs.getString("password_hash");
                user.accessLvl = rs.getInt("access_lvl");
                users.add(user);
            }
        }
        return users;
    }

    private List<Function> fetchFunctionsWithSort(String orderBy) throws SQLException {
        String sql = "SELECT id, name, user_id, func_result FROM functions " + orderBy;
        List<Function> functions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Function function = new Function();
                function.id = rs.getLong("id");
                function.name = rs.getString("name");
                function.userId = rs.getLong("user_id");
                function.funcResult = rs.getString("func_result");
                functions.add(function);
            }
        }
        return functions;
    }

    private List<Point> fetchPointsWithSort(String orderBy) throws SQLException {
        String sql = "SELECT id, x, y, index_in_function, function_id FROM points " + orderBy;
        List<Point> points = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Point point = new Point();
                point.id = rs.getLong("id");
                point.x = rs.getDouble("x");
                point.y = rs.getDouble("y");
                point.indexInFunction = rs.getInt("index_in_function");
                point.functionId = rs.getLong("function_id");
                points.add(point);
            }
        }
        return points;
    }

    // Методы для получения всех данных и сортировки в памяти
    private List<User> fetchAllUsersAndSort(Comparator<User> comparator) throws SQLException {
        List<User> users = fetchAllUsers();
        users.sort(comparator);
        return users;
    }

    private List<User> fetchAllUsers() throws SQLException {
        String sql = "SELECT id, name, password_hash, access_lvl FROM users";
        List<User> users = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.id = rs.getLong("id");
                user.name = rs.getString("name");
                user.passwordHash = rs.getString("password_hash");
                user.accessLvl = rs.getInt("access_lvl");
                users.add(user);
            }
        }
        return users;
    }

    private List<Function> fetchAllFunctionsAndSort(Comparator<Function> comparator) throws SQLException {
        List<Function> functions = fetchAllFunctions();
        functions.sort(comparator);
        return functions;
    }

    private List<Function> fetchAllFunctions() throws SQLException {
        String sql = "SELECT id, name, user_id, func_result FROM functions";
        List<Function> functions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Function function = new Function();
                function.id = rs.getLong("id");
                function.name = rs.getString("name");
                function.userId = rs.getLong("user_id");
                function.funcResult = rs.getString("func_result");
                functions.add(function);
            }
        }
        return functions;
    }

    private List<Point> fetchAllPointsAndSort(Comparator<Point> comparator) throws SQLException {
        List<Point> points = fetchAllPoints();
        points.sort(comparator);
        return points;
    }

    private List<Point> fetchAllPoints() throws SQLException {
        String sql = "SELECT id, x, y, index_in_function, function_id FROM points";
        List<Point> points = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Point point = new Point();
                point.id = rs.getLong("id");
                point.x = rs.getDouble("x");
                point.y = rs.getDouble("y");
                point.indexInFunction = rs.getInt("index_in_function");
                point.functionId = rs.getLong("function_id");
                points.add(point);
            }
        }
        return points;
    }

    private interface DbFetch<T> {
        List<T> get() throws SQLException;
    }

    private interface MemFetch<T> {
        List<T> get() throws Exception;
    }

    // Внутренние классы для представления данных
    static class User {
        long id;
        String name;
        String passwordHash;
        int accessLvl;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    static class Function {
        long id;
        String name;
        long userId;
        String funcResult;

        public String getName() {
            return name;
        }
    }

    static class Point {
        long id;
        double x;
        double y;
        int indexInFunction;
        long functionId;

        public double getX() {
            return x;
        }
    }
}