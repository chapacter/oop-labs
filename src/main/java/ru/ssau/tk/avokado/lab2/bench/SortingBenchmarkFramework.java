package ru.ssau.tk.avokado.lab2.bench;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.PointRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SortingBenchmarkFramework implements CommandLineRunner {

    private final DbPopulatorFramework populator;
    private final FunctionRepository functionRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    public SortingBenchmarkFramework(DbPopulatorFramework populator,
                                     FunctionRepository functionRepository,
                                     UserRepository userRepository,
                                     PointRepository pointRepository) {
        this.populator = populator;
        this.functionRepository = functionRepository;
        this.userRepository = userRepository;
        this.pointRepository = pointRepository;
    }

    @Override
    public void run(String... args) {
        try {
            runBench();
        } catch (Throwable t) {
            System.err.println("SortingBenchmarkFramework failed: " + t.getMessage());
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

        System.out.println("Starting extended sorting benchmarks...");

        runBoth("sort_users_by_name_asc",
                () -> userRepository.findAll(Sort.by(Sort.Direction.ASC, "name")),
                () -> {
                    List<User> all = userRepository.findAll();
                    all.sort(Comparator.comparing(User::getName, Comparator.nullsFirst(String::compareTo)));
                    return all;
                }, warmup, measured);

        runBoth("sort_users_by_name_desc",
                () -> userRepository.findAll(Sort.by(Sort.Direction.DESC, "name")),
                () -> {
                    List<User> all = userRepository.findAll();
                    all.sort(Comparator.comparing(User::getName, Comparator.nullsFirst(String::compareTo)).reversed());
                    return all;
                }, warmup, measured);

        runBoth("sort_users_by_id_asc",
                () -> userRepository.findAll(Sort.by(Sort.Direction.ASC, "id")),
                () -> {
                    List<User> all = userRepository.findAll();
                    all.sort(Comparator.comparing(User::getId, Comparator.nullsFirst(Long::compareTo)));
                    return all;
                }, warmup, measured);

        runBoth("sort_functions_by_name_asc",
                () -> functionRepository.findAll(Sort.by(Sort.Direction.ASC, "name")),
                () -> {
                    List<FunctionEntity> all = functionRepository.findAll();
                    all.sort(Comparator.comparing(FunctionEntity::getName, Comparator.nullsFirst(String::compareTo)));
                    return all;
                }, warmup, measured);

        runBoth("sort_functions_by_user_id_asc",
                () -> functionRepository.findAll(Sort.by(Sort.Direction.ASC, "user.id")),
                () -> {
                    List<FunctionEntity> all = functionRepository.findAll();
                    all.sort(Comparator.comparing(f -> Optional.ofNullable(f.getUser()).map(User::getId).orElse(-1L)));
                    return all;
                }, warmup, measured);

        runBoth("sort_points_by_x_asc",
                () -> pointRepository.findAll(Sort.by(Sort.Direction.ASC, "x")),
                () -> {
                    List<TabulatedPoint> all = pointRepository.findAll();
                    all.sort(Comparator.comparingDouble(TabulatedPoint::getX));
                    return all;
                }, warmup, measured);

        runBoth("sort_points_by_function_id_asc",
                () -> pointRepository.findAll(Sort.by(Sort.Direction.ASC, "function.id")),
                () -> {
                    List<TabulatedPoint> all = pointRepository.findAll();
                    all.sort(Comparator.comparing(p -> Optional.ofNullable(p.getFunction()).map(FunctionEntity::getId).orElse(-1L)));
                    return all;
                }, warmup, measured);

        System.out.println("All sorting benchmarks finished. CSVs and aggregates written to project root.");
    }

    private <T> void runBoth(String name, DbFetch<T> dbFetch, MemFetch<T> memFetch, int warmup, int measured) throws Exception {
        System.out.println("Benchmarking " + name + " (warmup=" + warmup + " measured=" + measured + ")");

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

        synchronized (SortingBenchmarkFramework.class) {
            boolean exists = new java.io.File("aggregates_sorting.csv").exists();
            try (PrintWriter pw = new PrintWriter(new FileWriter("aggregates_sorting.csv", true))) {
                if (!exists) pw.println("test,impl,n,mean_ns,median_ns,std_ns");
                pw.println(String.format("%s,db,%d,%d,%d", name, dbTimes.size(), aggDb.get("mean"), aggDb.get("median"), aggDb.get("std")));
                pw.println(String.format("%s,memory,%d,%d,%d", name, memTimes.size(), aggMem.get("mean"), aggMem.get("median"), aggMem.get("std")));
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

    private interface DbFetch<T> {
        List<T> get();
    }

    private interface MemFetch<T> {
        List<T> get();
    }
}
