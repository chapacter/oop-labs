package ru.ssau.tk.avokado.lab2.concurrent;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

public class ReadTask implements Runnable {
    private final TabulatedFunction function;

    public ReadTask(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }
        this.function = function;
    }

    @Override
    public void run() {
        int count = function.getCount();
        for (int i = 0; i < count; i++) {
            double x = function.getX(i);
            double y = function.getY(i);
            System.out.printf("After read: i = %d, x = %f, y = %f%n", i, x, y);
        }

    }
}
