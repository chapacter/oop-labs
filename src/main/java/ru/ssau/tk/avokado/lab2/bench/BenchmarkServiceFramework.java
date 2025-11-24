package ru.ssau.tk.avokado.lab2.bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class BenchmarkServiceFramework implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkServiceFramework.class);

    private final DbPopulatorFramework populator;
    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;

    public BenchmarkServiceFramework(DbPopulatorFramework populator,
                                     UserRepository userRepository,
                                     FunctionRepository functionRepository) {
        this.populator = populator;
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        int totalFunctions = 10_000;
        int batchSize = 500;
        int warmup = 200;
        int measured = 500;

        if (!populator.isPopulated(totalFunctions)) {
            populator.populate(totalFunctions, 2,  batchSize);
        } else {
            logger.info("DB already has >= {} functions (JPA).", totalFunctions);
        }

        runJpaBench("findUserByName_jpa", warmup, measured, this::opFindUserByName);

        runJpaBench("findFunctionByName_jpa", warmup, measured, this::opFindFunctionByName);

        runJpaBench("loadFunctionAndPoints_jpa", warmup, measured, this::opLoadFunctionAndPoints);

        logger.info("Framework-only benchmarks finished. CSV files in project root.");
    }

    private interface Action { void run(); }
    private void runJpaBench(String testName, int warmup, int measured, Action action) {
        logger.info("Benchmarking {} (warmup={} measured={})", testName, warmup, measured);

        for (int i = 0; i < warmup; i++) {
            try { action.run(); } catch (Exception ex) {}
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
                pw.println(testName + ",jpa," + i + "," + times.get(i));
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
            return copy.get(n/2);
        } else {
            return (copy.get(n/2 - 1) + copy.get(n/2)) / 2;
        }
    }

    @Transactional(readOnly = true)
    void opFindUserByName() {
        userRepository.findByName("seed_user");
    }

    @Transactional(readOnly = true)
    void opFindFunctionByName() {
        functionRepository.findByName("func_0");
    }

    @Transactional(readOnly = true)
    void opLoadFunctionAndPoints() {
        functionRepository.findByName("func_0").ifPresent(f -> {
            try {
                Object pts = f.getClass().getMethod("getPoints").invoke(f);
                if (pts instanceof java.util.Collection) {
                    ((java.util.Collection<?>) pts).size();
                }
            } catch (Exception ignored) {
            }
        });
    }
}
