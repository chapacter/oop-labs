package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.*;
import java.io.*;
import java.util.Arrays;

public final class TabulatedFunctionFileWriter {

    private TabulatedFunctionFileWriter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) {
        try (
                FileWriter fw1 = new FileWriter("output/array function.txt");
                FileWriter fw2 = new FileWriter("output/linked list function.txt");
                BufferedWriter bw1 = new BufferedWriter(fw1);
                BufferedWriter bw2 = new BufferedWriter(fw2)
        ) {

            double[] xArr = {0.0, 1.0, 2.0};
            double[] yArr = {0.0, 1.0, 4.0};
            TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xArr, yArr);

            double[] xList = {0.0, 0.5, 1.0, 1.5};
            double[] yList = {0.0, 0.25, 1.0, 2.25};
            TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xList, yList);

            FunctionsIO.writeTabulatedFunction(bw1, arrayFunction);
            FunctionsIO.writeTabulatedFunction(bw2, linkedListFunction);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
