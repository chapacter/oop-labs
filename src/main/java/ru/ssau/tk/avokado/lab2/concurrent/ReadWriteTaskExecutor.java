package ru.ssau.tk.avokado.lab2.concurrent;

import ru.ssau.tk.avokado.lab2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.ConstantFunction;

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        TabulatedFunction function = new LinkedListTabulatedFunction(new ConstantFunction(-1), 1, 1000, 10000);
        ReadTask rTask = new ReadTask(function);
        WriteTask wTask = new WriteTask(function, 0.5);
        new Thread(rTask).start(); // или их надо вызывать в обратном порядке?
        new Thread(wTask).start();
    }
}
