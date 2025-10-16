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
        for (int i = 0; i < function.getCount(); i++) {
            System.out.printf("After read: i = %d, x = %f, y = %f%n", i, function.getX(i), function.getY(i));
        }
    }
}
