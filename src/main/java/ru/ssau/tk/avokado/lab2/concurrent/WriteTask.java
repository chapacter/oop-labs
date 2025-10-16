package ru.ssau.tk.avokado.lab2.concurrent;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

public class WriteTask implements Runnable {
    private final TabulatedFunction function;
    private final double value;

    public WriteTask(TabulatedFunction function, double value) {
        if (function == null) { throw new IllegalArgumentException("Function == null");}
//        if (value == null) { throw new IllegalArgumentException("Value == null");}

        this.function = function;
        this.value = value;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); i++) {
            function.setY(i, value);
            System.out.printf("Writing for index %d complete%n", i);
        }
    }
}
