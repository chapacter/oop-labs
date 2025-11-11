package ru.ssau.tk.avokado.lab2.concurrent;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplyingTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MultiplyingTask.class);
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("function is null");
        }
        this.function = function;
        logger.debug("MultiplyingTask создан для функции (count = {})", function.getCount());
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        logger.info("Поток {} начал MultiplyingTask", threadName);
        int n = function.getCount();
        for (int i = 0; i < n; i++) {
            synchronized (function) {
                double oldY = function.getY(i);
                function.setY(i, oldY * 2.0);
            }
        }
        logger.info("Поток {} закончил MultiplyingTask", threadName);
        System.out.println("Thread " + Thread.currentThread().getName() + " finished MultiplyingTask");
    }
}
