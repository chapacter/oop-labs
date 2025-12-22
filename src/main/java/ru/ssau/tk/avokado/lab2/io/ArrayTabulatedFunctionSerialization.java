package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.operations.TabulatedDifferentialOperator;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ArrayTabulatedFunctionSerialization {

    private ArrayTabulatedFunctionSerialization() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) {
        Path outDir = Paths.get("output");
        Path file = outDir.resolve("serialized array functions.bin");

        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 4.0, 9.0}; // f(x) = x^2
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(x, y);

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction firstDerivative = operator.derive(original);
        TabulatedFunction secondDerivative = operator.derive(firstDerivative);

        try (FileOutputStream fos = new FileOutputStream(file.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            FunctionsIO.serialize(bos, original);
            FunctionsIO.serialize(bos, firstDerivative);
            FunctionsIO.serialize(bos, secondDerivative);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream(file.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            TabulatedFunction a = FunctionsIO.deserialize(bis);
            TabulatedFunction b = FunctionsIO.deserialize(bis);
            TabulatedFunction c = FunctionsIO.deserialize(bis);

            System.out.println("Original: " + a.toString());
            System.out.println("First derivative: " + b.toString());
            System.out.println("Second derivative: " + c.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
