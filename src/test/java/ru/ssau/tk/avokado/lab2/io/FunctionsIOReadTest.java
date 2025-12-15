package ru.ssau.tk.avokado.lab2.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionsIOReadTest {

    private static final double EPS = 1e-12;

    @Test
    public void testReadValidFunctionWithCommaDecimal() throws IOException {
        String data = "3\n" +
                "0,0 0,0\n" +
                "1,0 1,0\n" +
                "2,5 6,25\n";
        BufferedReader br = new BufferedReader(new StringReader(data));
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        TabulatedFunction f = FunctionsIO.readTabulatedFunction(br, factory);

        assertEquals(3, f.getCount());
        assertEquals(0.0, f.getX(0), EPS);
        assertEquals(0.0, f.getY(0), EPS);
        assertEquals(1.0, f.getX(1), EPS);
        assertEquals(1.0, f.getY(1), EPS);
        assertEquals(2.5, f.getX(2), EPS);
        assertEquals(6.25, f.getY(2), EPS);
    }

    @Test
    public void testReadInvalidNumberCausesIOException() {
        String data = "2\n" +
                "1,0 1,0\n" +
                "2,0 abc\n";
        BufferedReader br = new BufferedReader(new StringReader(data));
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        assertThrows(IOException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction(br, factory);
            }
        });
    }

    @Test
    public void testNullArgumentsThrow() {
        BufferedReader br = new BufferedReader(new StringReader("0\n"));
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

//        assertThrows(IllegalArgumentException.class, new Executable() {
//            @Override
//            public void execute() throws Throwable {
//                FunctionsIO.readTabulatedFunction(null, factory);
//            }
//        });

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction(br, null);
            }
        });
    }

    @Test
    public void testReadUnexpectedEOF() {
        String data = "3\n" +
                "1,0 1,0\n"; // only one point but count = 3
        BufferedReader br = new BufferedReader(new StringReader(data));
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        assertThrows(IOException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction(br, factory);
            }
        });
    }
}
