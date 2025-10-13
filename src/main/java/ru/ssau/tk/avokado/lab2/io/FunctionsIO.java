package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {
    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class");
    }

    static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(function.getCount());

        for (Point point : function) {
            dataOutputStream.writeDouble(point.x);
            dataOutputStream.writeDouble(point.y);
        }
        dataOutputStream.flush();
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) {
        if (writer == null) {
            throw new IllegalArgumentException("writer is null");
        }
        if (function == null) {
            throw new IllegalArgumentException("function is null");
        }

        PrintWriter pw = new PrintWriter(writer);

        pw.println(function.getCount());

        for (Point p : function) {
            pw.printf("%f %f\n", p.x, p.y);
        }

        pw.flush();
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream,
                                                          TabulatedFunctionFactory factory) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        int count = dataInputStream.readInt();
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = dataInputStream.readDouble();
            yValues[i] = dataInputStream.readDouble();
        }

        return factory.create(xValues, yValues);
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader,
                                                          TabulatedFunctionFactory factory) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory is null");
        }

        String firstLine = reader.readLine();
        if (firstLine == null) {
            throw new IOException("Empty input: expected count");
        }

        int count;
        try {
            count = Integer.parseInt(firstLine.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid count: " + firstLine, e);
        }

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of input at line for point " + i);
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                throw new IOException("Empty line for point " + i);
            }
            String[] parts = trimmed.split("\\s+");
            if (parts.length < 2) {
                throw new IOException("Expected two numbers separated by space at line for point " + i);
            }
            try {
                Number nx = nf.parse(parts[0]);
                Number ny = nf.parse(parts[1]);
                xValues[i] = nx.doubleValue();
                yValues[i] = ny.doubleValue();
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }

        return factory.create(xValues, yValues);
    }


    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(function);
        oos.flush();
    }

    static TabulatedFunction deserialize(BufferedInputStream stream)throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        return (TabulatedFunction) objectInputStream.readObject();
    }
}
