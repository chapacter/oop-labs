package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.operations.TabulatedDifferentialOperator;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class TabulatedFunctionFileInputStream {
    public static void main(String[] args) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream("input/binary function.bin"))) {
            TabulatedFunction function = FunctionsIO.readTabulatedFunction(inputStream, new ArrayTabulatedFunctionFactory());
            System.out.println("Функция, считанная из файла:");
            System.out.println(function);

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        try {
            System.out.println("Введите размер и значения функции:");
            BufferedInputStream consoleInput = new BufferedInputStream(System.in);

            TabulatedFunction functionFromConsole = FunctionsIO.readTabulatedFunction(consoleInput, new LinkedListTabulatedFunctionFactory());

            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());
            TabulatedFunction derivative = operator.derive(functionFromConsole);

            System.out.println("Производная функции:");
            System.out.println(derivative);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
