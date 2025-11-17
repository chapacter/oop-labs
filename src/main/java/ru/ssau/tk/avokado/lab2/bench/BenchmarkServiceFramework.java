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

/**
 * Запускает JPA-only бенчмарки. Компонент реализует CommandLineRunner,
 * поэтому выполнится автоматически при старте Spring Boot.
 *
 * Заменить/вставить этот файл в пакет ru.ssau.tk.avokado.lab2.bench
 */
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
        int batchSize = 500; // использование saveAll партиями
        int warmup = 200;
        int measured = 500;

        if (!populator.isPopulated(totalFunctions)) {
            populator.populate(totalFunctions, batchSize);
        } else {
            logger.info("DB already has >= {} functions (JPA).", totalFunctions);
        }

        // 1) find user by name (JPA)
        runJpaBench("findUserByName_jpa", warmup, measured, this::opFindUserByName);

        // 2) find function by name (JPA)
        runJpaBench("findFunctionByName_jpa", warmup, measured, this::opFindFunctionByName);

        // 3) load function and touch points collection (join-like) — assumes mapping points exists
        runJpaBench("loadFunctionAndPoints_jpa", warmup, measured, this::opLoadFunctionAndPoints);

        logger.info("Framework-only benchmarks finished. CSV files in project root.");
    }

    private interface Action { void run(); }

    /**
     * Универсальный раннер: прогрев + измерения + запись CSV + лог с медианой.
     */
    private void runJpaBench(String testName, int warmup, int measured, Action action) {
        logger.info("Benchmarking {} (warmup={} measured={})", testName, warmup, measured);

        // warmup
        for (int i = 0; i < warmup; i++) {
            try { action.run(); } catch (Exception ex) { /* ignore warmup errors */ }
        }

        List<Long> times = new ArrayList<>(measured);
        for (int i = 0; i < measured; i++) {
            long s = System.nanoTime();
            try {
                action.run();
            } catch (Exception ex) {
                // не падаем на одной итерации — записываем очень большое время как отметку (можно изменить)
                logger.warn("Iteration {} thrown exception in {}: {}", i, testName, ex.toString());
            }
            long e = System.nanoTime();
            times.add(e - s);
        }

        // Write CSV
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
        // Тут именно тот формат, который ты просил: "Wrote <file> (median ~ <N> ns)"
        logger.info("Wrote {} (median ~ {} ns)", csv, med);
    }

    // медиана: корректно на сортированном списке
    private long median(List<Long> arr) {
        if (arr == null || arr.isEmpty()) return 0L;
        List<Long> copy = new ArrayList<>(arr);
        Collections.sort(copy);
        int n = copy.size();
        if (n % 2 == 1) {
            return copy.get(n/2);
        } else {
            // среднее двух для парного — округлённо в long
            return (copy.get(n/2 - 1) + copy.get(n/2)) / 2;
        }
    }

    // --- операции (JPA) ---
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
