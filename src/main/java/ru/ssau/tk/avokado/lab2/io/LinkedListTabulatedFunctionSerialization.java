package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.operations.TabulatedDifferentialOperator;

import java.io.*;

public class LinkedListTabulatedFunctionSerialization {
    public static void main(String[] args) {
        // Запись функций в файл
        try (FileOutputStream fileOutputStream = new FileOutputStream("output/serialized linked list functions.bin")) {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            // Создать табулированную функцию типа LinkedListTabulatedFunction
            double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
            double[] yValues = {1.0, 4.0, 9.0, 16.0, 25.0}; // y = x^2
            LinkedListTabulatedFunction originalFunction = new LinkedListTabulatedFunction(xValues, yValues);

            // Найти первую и вторую производные с помощью TabulatedDifferentialOperator
            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());
            LinkedListTabulatedFunction firstDerivative = (LinkedListTabulatedFunction) operator.derive(originalFunction);
            LinkedListTabulatedFunction secondDerivative = (LinkedListTabulatedFunction) operator.derive(firstDerivative);

            // Произвести сериализацию всех трёх функций
            FunctionsIO.serialize(bufferedOutputStream, originalFunction);
            FunctionsIO.serialize(bufferedOutputStream, firstDerivative);
            FunctionsIO.serialize(bufferedOutputStream, secondDerivative);

            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Чтение функций из файла
        try (FileInputStream fileInputStream = new FileInputStream("output/serialized linked list functions.bin")) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // Десериализовать все три функции
            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bufferedInputStream);
            TabulatedFunction deserializedFirst = FunctionsIO.deserialize(bufferedInputStream);
            TabulatedFunction deserializedSecond = FunctionsIO.deserialize(bufferedInputStream);

            // Вывести значения всех функций в консоль
            System.out.println("Original function: " + deserializedOriginal.toString());
            System.out.println("First derivative: " + deserializedFirst.toString());
            System.out.println("Second derivative: " + deserializedSecond.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}