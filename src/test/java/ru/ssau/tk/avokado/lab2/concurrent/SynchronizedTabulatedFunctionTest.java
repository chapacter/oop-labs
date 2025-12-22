package ru.ssau.tk.avokado.lab2.concurrent;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
            assertEquals(x[idx], p.x(), 1e-12);
            assertEquals(y[idx], p.y(), 1e-12);
            idx++;
        }
        assertEquals(3, idx);

        idx = 0;
        for (Point p : sync) {
            assertEquals(x[idx], p.x(), 1e-12);
            assertEquals(y[idx], p.y(), 1e-12);
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
        assertEquals(syncWithMutex, syncOtherMutex);
        assertEquals(syncOtherMutex, syncWithMutex);
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

        assertNotEquals(sync1, sync2);

        Object someObject = new Object();
        assertNotEquals(sync1, someObject);
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


    @Test
    void testIteratorIsImmutableSnapshot() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        Iterator<Point> iterator = sync.iterator();

        sync.setY(0, 100.0);
        sync.setY(1, 200.0);
        sync.setY(2, 300.0);

        assertTrue(iterator.hasNext());
        Point point1 = iterator.next();
        assertEquals(1.0, point1.x(), 1e-12);
        assertEquals(10.0, point1.y(), 1e-12);

        Point point2 = iterator.next();
        assertEquals(2.0, point2.x(), 1e-12);
        assertEquals(20.0, point2.y(), 1e-12);

        Point point3 = iterator.next();
        assertEquals(3.0, point3.x(), 1e-12);
        assertEquals(30.0, point3.y(), 1e-12);

        assertFalse(iterator.hasNext());
    }

    @Test
    void testMultipleIteratorsAreIndependent() {
        double[] x = {1.0, 2.0};
        double[] y = {10.0, 20.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        Iterator<Point> iterator1 = sync.iterator();
        Iterator<Point> iterator2 = sync.iterator();

        Point p1_1 = iterator1.next();
        Point p1_2 = iterator1.next();
        assertFalse(iterator1.hasNext());

        assertTrue(iterator2.hasNext());
        Point p2_1 = iterator2.next();
        assertEquals(p1_1.x(), p2_1.x(), 1e-12);
        assertEquals(p1_1.y(), p2_1.y(), 1e-12);

        assertTrue(iterator2.hasNext());
        Point p2_2 = iterator2.next();
        assertEquals(p1_2.x(), p2_2.x(), 1e-12);
        assertEquals(p1_2.y(), p2_2.y(), 1e-12);

        assertFalse(iterator2.hasNext());
    }

    @Test
    void testIteratorNoConcurrentModification() {
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] y = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        int count = 0;
        for (Point point : sync) {
            sync.setY(count, -1.0);

            assertEquals(x[count], point.x(), 1e-12);
            assertEquals(y[count], point.y(), 1e-12);

            count++;
        }
        assertEquals(4, count);
    }

    @Test
    void testIteratorNextBeyondBoundsThrowsException() {
        double[] x = {1.0, 2.0};
        double[] y = {10.0, 20.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        Iterator<Point> iterator = sync.iterator();

        iterator.next();
        iterator.next();

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testIteratorWithSingleElement() {
        double[] x = new double[]{5.0, 6.0};
        double[] y = y = new double[]{50.0, 60.0};

        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        Iterator<Point> iterator = sync.iterator();
        assertTrue(iterator.hasNext());

        int count = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(x[count], point.x(), 1e-12);
            assertEquals(y[count], point.y(), 1e-12);
            count++;
        }

        assertEquals(x.length, count);
    }
}
