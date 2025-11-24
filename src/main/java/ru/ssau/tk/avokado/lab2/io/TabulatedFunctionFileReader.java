package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class TabulatedFunctionFileReader {

    private TabulatedFunctionFileReader() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) {
        try (FileReader fr1 = new FileReader("input/function.txt");
             FileReader fr2 = new FileReader("input/function.txt");
             BufferedReader br1 = new BufferedReader(fr1);
             BufferedReader br2 = new BufferedReader(fr2)) {

            TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunctionFactory listFactory = new LinkedListTabulatedFunctionFactory();

            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(br1, arrayFactory);
            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(br2, listFactory);

            System.out.println(arrayFunction.toString());
            System.out.println(linkedListFunction.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}