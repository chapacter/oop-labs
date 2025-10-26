package ru.ssau.tk.avokado.lab2.concurrent;

import ru.ssau.tk.avokado.lab2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.UnitFunction;

import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {
        final double xFrom = 1.0;
        final double xTo = 1000.0;
        final int count = 1000;

        TabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), xFrom, xTo, count);

        List<Thread> threads = new ArrayList<Thread>();

        final int threadsCount = 10;
        for (int i = 0; i < threadsCount; i++) {
            Thread t = new Thread(new MultiplyingTask(function), "Multiplier-" + i);
            threads.add(t);
        }

        for (Thread t : threads) {
            t.start();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Result of function");
        for (int i = 0; i < (function.getCount()); i++) {
            System.out.printf("i=%d x=%.6f y=%.6f%n", i, function.getX(i), function.getY(i));
        }
    }
}