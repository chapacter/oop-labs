package ru.ssau.tk.avokado.lab2.concurrent;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizedTabulatedFunctionTest {

    @Test
    void testConstructorNullThrows() {
        try {
            new SynchronizedTabulatedFunction(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    void testGettersAndBoundsAndCount() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};
        TabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        assertEquals(3, sync.getCount());
        assertEquals(1.0, sync.leftBound(), 1e-12);
        assertEquals(3.0, sync.rightBound(), 1e-12);

        assertEquals(2.0, sync.getY(0), 1e-12);
        assertEquals(4.0, sync.getY(1), 1e-12);
        assertEquals(3.0, sync.getX(2), 1e-12);
    }

    @Test
    void testSetYReflectsUnderlying() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        sync.setY(1, 123.0);
        assertEquals(123.0, sync.getY(1), 1e-12);
        // underlying changed as well
        assertEquals(123.0, base.getY(1), 1e-12);
    }

    @Test
    void testIndexOfMethods() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {5.0, 10.0, 15.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(f);

        assertEquals(2, sync.indexOfX(2.0));
        assertEquals(-1, sync.indexOfX(3.0));
        assertEquals(1, sync.indexOfY(10.0));
        assertEquals(-1, sync.indexOfY(999.0));
    }

    @Test
    void testApplyAndInterpolation() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(f);

        assertEquals(0.0, sync.apply(0.0), 1e-12);
        assertEquals(0.5, sync.apply(0.5), 1e-12);
        assertEquals(4.0, sync.apply(2.0), 1e-12);
    }

    @Test
    void testIteratorWhileAndForEach() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        Iterator<Point> it = sync.iterator();
        int idx = 0;
        while (it.hasNext()) {
            Point p = it.next();
            assertEquals(x[idx], p.x, 1e-12);
            assertEquals(y[idx], p.y, 1e-12);
            idx++;
        }
        assertEquals(3, idx);

        idx = 0;
        for (Point p : sync) {
            assertEquals(x[idx], p.x, 1e-12);
            assertEquals(y[idx], p.y, 1e-12);
            idx++;
        }
        assertEquals(3, idx);
    }


    @Test
    void testConstructorWithMutexEqualsHashCodeToString() {
        double[] x = {1.0, 2.0};
        double[] y = {10.0, 20.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);

        Object customMutex = new Object();
        SynchronizedTabulatedFunction syncWithMutex = new SynchronizedTabulatedFunction(base, customMutex);

        String baseStr = base.toString();
        String syncStr = syncWithMutex.toString();
        assertEquals(baseStr, syncStr);
        assertEquals(base.hashCode(), syncWithMutex.hashCode());

        SynchronizedTabulatedFunction syncOtherMutex = new SynchronizedTabulatedFunction(base, new Object());
        assertTrue(syncWithMutex.equals(syncOtherMutex));
        assertTrue(syncOtherMutex.equals(syncWithMutex));
    }

    @Test
    void testEqualsDifferentDelegateAndDifferentType() {
        double[] x1 = {0.0, 1.0};
        double[] y1 = {0.0, 1.0};
        double[] x2 = {0.0, 1.0, 2.0};
        double[] y2 = {0.0, 1.0, 4.0};

        ArrayTabulatedFunction base1 = new ArrayTabulatedFunction(x1, y1);
        ArrayTabulatedFunction base2 = new ArrayTabulatedFunction(x2, y2);

        SynchronizedTabulatedFunction sync1 = new SynchronizedTabulatedFunction(base1);
        SynchronizedTabulatedFunction sync2 = new SynchronizedTabulatedFunction(base2);

        assertFalse(sync1.equals(sync2));

        Object someObject = new Object();
        assertFalse(sync1.equals(someObject));
    }

    @Test
    void testIteratorRemoveThrowsUnsupportedOperation() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        Iterator<?> it = sync.iterator();
        assertTrue(it.hasNext());
        it.next();

        try {
            it.remove();
            fail("Expected UnsupportedOperationException from iterator.remove()");
        } catch (UnsupportedOperationException expected) {
        }
    }
}
