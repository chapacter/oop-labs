package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TabulatedFunctionFileOutputStream {
    public static void main(String[] args) {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {1.5, 32.4, 56.2, 23.0, 1.0};

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        try (BufferedOutputStream arrayStream = new BufferedOutputStream(new FileOutputStream("output/array function.bin"));
             BufferedOutputStream linkedListStream = new BufferedOutputStream(new FileOutputStream("output/linked list function.bin"))
        ) {
            FunctionsIO.writeTabulatedFunction(arrayStream, arrayFunction);
            FunctionsIO.writeTabulatedFunction(linkedListStream, linkedListFunction);

            System.out.println("Функции успешно записаны в файлы:");
            System.out.println("- output/array function.bin");
            System.out.println("- output/linked list function.bin");
        } catch (IOException e) {
            System.err.println("Ошибка при записи файлов:");
            e.printStackTrace();
        }
    }
}