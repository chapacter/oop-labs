package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionIteratorTest {

    @Test
    public void testIteratorWhileLoop() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Iterator<Point> it = f.iterator();
        int idx = 0;
        while (it.hasNext()) {
            Point p = it.next();
            assertEquals(x[idx], p.x(), 1e-12);
            assertEquals(y[idx], p.y(), 1e-12);
            idx++;
        }
        assertEquals(3, idx);

        try {
            it.next();
            fail("Expected NoSuchElementException after iterator exhausted");
        } catch (java.util.NoSuchElementException ignored) {
        }
    }

    @Test
    public void testIteratorForEachLoop() {
        double[] x = {5.0, 6.0, 7.0, 8.0};
        double[] y = {50.0, 60.0, 70.0, 80.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        int idx = 0;
        for (Point p : f) {
            assertEquals(x[idx], p.x(), 1e-12);
            assertEquals(y[idx], p.y(), 1e-12);
            idx++;
        }
        assertEquals(4, idx);
    }

    @Test
    public void testIteratorRemoveUnsupported() {
        double[] x = {1.0, 2.0};
        double[] y = {10.0, 20.0};
        final ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        final Iterator<Point> it = f.iterator();
        assertThrows(UnsupportedOperationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                it.remove();
            }
        });
    }

    @Test
    public void testTwoIteratorsAreIndependent() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Iterator<Point> a = f.iterator();
        Iterator<Point> b = f.iterator();

        Point pa1 = a.next();
        assertEquals(1.0, pa1.x(), 1e-12);
        Point pa2 = a.next();
        assertEquals(2.0, pa2.x(), 1e-12);

        Point pb1 = b.next();
        assertEquals(1.0, pb1.x(), 1e-12);

        Point pa3 = a.next();
        assertEquals(3.0, pa3.x(), 1e-12);

        Point pb2 = b.next();
        assertEquals(2.0, pb2.x(), 1e-12);
    }

    @Test
    public void testHasNextMultipleCallsAndNextAfterExhaustionThrows() {
        double[] x = {7.0, 8.0};
        double[] y = {70.0, 80.0};
        final ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Iterator<Point> it = f.iterator();

        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        Point p0 = it.next();
        assertEquals(7.0, p0.x(), 1e-12);

        assertTrue(it.hasNext());
        Point p1 = it.next();
        assertEquals(8.0, p1.x(), 1e-12);

        assertFalse(it.hasNext());
        assertFalse(it.hasNext());

        assertThrows(java.util.NoSuchElementException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                it.next();
            }
        });
    }
}
