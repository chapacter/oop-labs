package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsIO.class);

    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Байтовая запись (DataOutputStream)
    static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        if (outputStream == null) {
            logger.error("writeTabulatedFunction (binary): outputStream == null");
            throw new IllegalArgumentException("outputStream is null");
        }
        if (function == null) {
            logger.error("writeTabulatedFunction (binary): function == null");
            throw new IllegalArgumentException("function is null");
        }

        logger.info("Запись табулированной функции (байтовая) — точек: {}", function.getCount());
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(function.getCount());

        for (Point point : function) {
            dataOutputStream.writeDouble(point.x);
            dataOutputStream.writeDouble(point.y);
        }
        dataOutputStream.flush();
        logger.debug("writeTabulatedFunction (binary): данные успешно записаны и сброшены");
    }

    // Текстовая запись (BufferedWriter)
    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) {
        if (writer == null) {
            logger.error("writeTabulatedFunction (text): writer == null");
            throw new IllegalArgumentException("writer is null");
        }
        if (function == null) {
            logger.error("writeTabulatedFunction (text): function == null");
            throw new IllegalArgumentException("function is null");
        }

        logger.info("Запись табулированной функции (текстовая) — точек: {}", function.getCount());
        PrintWriter pw = new PrintWriter(writer);

        pw.println(function.getCount());

        for (Point p : function) {
            pw.printf("%f %f%n", p.x, p.y);
        }
        pw.flush();
        logger.debug("writeTabulatedFunction (text): данные записаны и сброшены");
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream,
                                                          TabulatedFunctionFactory factory) throws IOException {
        if (inputStream == null) {
            logger.error("readTabulatedFunction (binary): inputStream == null");
            throw new IllegalArgumentException("inputStream is null");
        }
        if (factory == null) {
            logger.error("readTabulatedFunction (binary): factory == null");
            throw new IllegalArgumentException("factory is null");
        }

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        int count = dataInputStream.readInt();
        logger.info("Чтение табулированной функции (байтовая) — ожидается {} точек", count);
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = dataInputStream.readDouble();
            yValues[i] = dataInputStream.readDouble();
        }
        logger.debug("readTabulatedFunction (binary): прочитано {} точек", count);
        return factory.create(xValues, yValues);
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader,
                                                          TabulatedFunctionFactory factory) throws IOException {
        if (reader == null) {
            logger.error("readTabulatedFunction (text): reader == null");
            throw new IllegalArgumentException("reader is null");
        }
        if (factory == null) {
            logger.error("readTabulatedFunction (text): factory == null");
            throw new IllegalArgumentException("factory is null");
        }

        String firstLine = reader.readLine();
        if (firstLine == null) {
            logger.error("readTabulatedFunction (text): пустой вход — ожидался count");
            throw new IOException("Empty input: expected count");
        }

        int count;
        try {
            count = Integer.parseInt(firstLine.trim());
        } catch (NumberFormatException e) {
            logger.error("readTabulatedFunction (text): неверный формат count: {}", firstLine);
            throw new IOException("Invalid count: " + firstLine, e);
        }
        logger.info("Чтение табулированной функции (текст) — ожидается {} точек", count);

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line == null) {
                logger.error("readTabulatedFunction (text): неожиданный конец входа при точке {}", i);
                throw new IOException("Unexpected end of input at line for point " + i);
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                logger.error("readTabulatedFunction (text): пустая строка для точки {}", i);
                throw new IOException("Empty line for point " + i);
            }
            String[] parts = trimmed.split("\\s+");
            if (parts.length < 2) {
                logger.error("readTabulatedFunction (text): ожидается два числа в строке для точки {}: '{}'", i, line);
                throw new IOException("Expected two numbers separated by space at line for point " + i);
            }
            try {
                Number nx = nf.parse(parts[0]);
                Number ny = nf.parse(parts[1]);
                xValues[i] = nx.doubleValue();
                yValues[i] = ny.doubleValue();
            } catch (ParseException e) {
                logger.error("readTabulatedFunction (text): не удалось распарсить числа в строке '{}'", line, e);
                throw new IOException(e);
            }
        }
        logger.debug("readTabulatedFunction (text): прочитано {} точек", count);
        return factory.create(xValues, yValues);
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        if (stream == null) {
            logger.error("serialize: stream == null");
            throw new IllegalArgumentException("stream is null");
        }

        if (function == null) {
            logger.info("Сериализация функции (ObjectOutputStream): функция == null (записываем null)");
        } else {
            logger.info("Сериализация функции (ObjectOutputStream): точек = {}", function.getCount());
        }

        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(function);
        oos.flush();
        logger.debug("serialize: данные сериализованы и сброшены");
    }

    static TabulatedFunction deserialize(BufferedInputStream stream) throws IOException, ClassNotFoundException {
        if (stream == null) {
            logger.error("deserialize: stream == null");
            throw new IllegalArgumentException("stream is null");
        }
        logger.info("Десериализация функции (ObjectInputStream)");
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        TabulatedFunction tf = (TabulatedFunction) objectInputStream.readObject();
        logger.debug("deserialize: успешная десериализация (результат null? {})", tf == null);
        return tf;
    }
}
