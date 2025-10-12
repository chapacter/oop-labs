package ru.ssau.tk.avokado.lab2.io;

import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class FunctionsIO {
    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class");
    }

//    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) {
//        // Для Y
//    }

    static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(function.getCount());

        for (Point point : function) {
            dataOutputStream.writeDouble(point.x);
            dataOutputStream.writeDouble(point.y);
        }
        dataOutputStream.flush();
    }
}
