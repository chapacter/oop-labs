package ru.ssau.tk.avokado.lab2.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;

import java.io.*;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionsIOTest {

    private static final double EPS = 1e-12;

    @Test
    public void testWriteAndReadBinary() throws Exception {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);

        FunctionsIO.writeTabulatedFunction(bos, f);
        bos.flush();

        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(bais);

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction g = FunctionsIO.readTabulatedFunction(bis, factory);

        assertEquals(f.getCount(), g.getCount());
        for (int i = 0; i < f.getCount(); i++) {
            assertEquals(f.getX(i), g.getX(i), EPS);
            assertEquals(f.getY(i), g.getY(i), EPS);
        }
    }

//    @Test
//    public void testWriteTextAndInspect() throws Exception {
//        double[] x = {0.5, 1.5};
//        double[] y = {2.5, 3.5};
//        TabulatedFunction f = new ArrayTabulatedFunction(x, y);
//
//        StringWriter sw = new StringWriter();
//        BufferedWriter bw = new BufferedWriter(sw);
//
//        FunctionsIO.writeTabulatedFunction(bw, f);
//        bw.flush();
//
//        String out = sw.toString();
//        assertTrue(out.contains(String.valueOf(f.getCount())));
//        assertTrue(out.contains(String.format(Locale.ROOT, "%f %f", x[0], y[0])));
//        try {
//            bw.write("x");
//            bw.flush();
//        } catch (IOException e) {
//            fail("BufferedWriter should not be closed by writeTabulatedFunction");
//        }
//    }

    @Test
    public void testReadTextWithCommaDecimal() throws Exception {
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
    public void testReadTextInvalidCountThrows() {
        String data = "notANumber\n";
        BufferedReader br = new BufferedReader(new StringReader(data));
        final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        assertThrows(IOException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction(br, factory);
            }
        });
    }

    @Test
    public void testReadTextInvalidNumberThrowsIOException() {
        String data = "2\n" +
                "1,0 2,0\n" +
                "3,0 abc\n";
        BufferedReader br = new BufferedReader(new StringReader(data));
        final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        assertThrows(IOException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction(br, factory);
            }
        });
    }

    @Test
    public void testReadTextNullArgsThrow() {
        final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        BufferedReader br = new BufferedReader(new StringReader("0\n"));

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction((BufferedReader) null, factory);
            }
        });

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction(br, null);
            }
        });
    }

    @Test
    public void testReadBinaryUnexpectedEOFThrows() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        BufferedInputStream bis = new BufferedInputStream(bais);
        final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        assertThrows(IOException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.readTabulatedFunction(bis, factory);
            }
        });
    }

    @Test
    public void testSerializeAndDeserializeObjects() throws Exception {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};
        TabulatedFunction original = new ArrayTabulatedFunction(x, y);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);

        FunctionsIO.serialize(bos, original);
        bos.flush();

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BufferedInputStream bis = new BufferedInputStream(bais);
        TabulatedFunction restored = FunctionsIO.deserialize(bis);

        assertNotNull(restored);
        assertEquals(original.getCount(), restored.getCount());
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), restored.getX(i), EPS);
            assertEquals(original.getY(i), restored.getY(i), EPS);
        }
    }

    @Test
    public void testSerializeDoesNotCloseStreamAndAllowsNullFunction() throws Exception {
        class TrackableBAOS extends ByteArrayOutputStream {
            private boolean closed = false;
            @Override
            public void close() throws IOException {
                closed = true;
                super.close();
            }
            public boolean isClosed() { return closed; }
        }

        TrackableBAOS tbaos = new TrackableBAOS();
        BufferedOutputStream bos = new BufferedOutputStream(tbaos);

        FunctionsIO.serialize(bos, null);
        bos.flush();
        assertFalse(tbaos.isClosed(), "serialize should not close underlying stream");

        byte[] data = tbaos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BufferedInputStream bis = new BufferedInputStream(bais);
        Object obj = FunctionsIO.deserialize(bis);
        assertNull(obj, "Deserializing a serialized null should return null");
    }

    @Test
    public void testSerializeNullStreamThrows() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        final TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.serialize(null, func);
            }
        });
    }

    @Test
    public void testDeserializeNullStreamThrows() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.deserialize(null);
            }
        });
    }

    @Test
    public void testWriteTextNullArgsThrow() {
        final TabulatedFunction func = new ArrayTabulatedFunction(new double[]{0.0,1.0}, new double[]{0.0,1.0});
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.writeTabulatedFunction((BufferedWriter) null, func);
            }
        });
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.writeTabulatedFunction(new BufferedWriter(new StringWriter()), null);
            }
        });
    }

    @Test
    public void testWriteBinaryNullFunctionThrowsNPE() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                FunctionsIO.writeTabulatedFunction(bos, null);
            }
        });
    }
}
