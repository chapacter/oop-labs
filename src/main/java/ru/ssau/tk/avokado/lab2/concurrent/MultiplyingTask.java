package ru.ssau.tk.avokado.lab2.concurrent;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

public class MultiplyingTask implements Runnable {
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("function is null");
        }
        this.function = function;
    }

    @Override
    public void run() {
        int n = function.getCount();
        for (int i = 0; i < n; i++) {
            double oldY = function.getY(i);
            function.setY(i, oldY * 2.0);
        }
        System.out.println("Thread " + Thread.currentThread().getName() + " finished MultiplyingTask");
    }
}
