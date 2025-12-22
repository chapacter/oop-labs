package ru.ssau.tk.avokado.lab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ssau.tk.avokado.lab2.bench.DbPopulatorFramework;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final DbPopulatorFramework populator;

    public AdminController(DbPopulatorFramework populator) {
        this.populator = populator;
    }

    @PostMapping("/populate")
    public String populate(@RequestParam(defaultValue = "10000") int functions,
                           @RequestParam(defaultValue = "5") int pointsPerFunction,
                           @RequestParam(defaultValue = "500") int batchSize,
                           @RequestParam(defaultValue = "false") boolean async) {
        logger.info("POST /api/admin/populate functions={}, pointsPerFunction={}, batchSize={}, async={}", functions, pointsPerFunction, batchSize, async);
        if (async) {
            Thread t = new Thread(() -> {
                try {
                    populator.populate(functions, pointsPerFunction, batchSize);
                } catch (Throwable e) {
                    logger.error("Populate background task failed", e);
                }
            }, "db-populator-thread");
            t.setDaemon(true);
            t.start();
            return "Started populate (async): functions=" + functions + ", pointsPerFunction=" + pointsPerFunction;
        } else {
            populator.populate(functions, pointsPerFunction, batchSize);
            return "Populate finished: functions=" + functions + ", pointsPerFunction=" + pointsPerFunction;
        }
    }

    @PostMapping("/clear")
    public String clear() {
        logger.info("POST /api/admin/clear");
        populator.clearAll();
        return "Cleared DB tables via populator";
    }
}
