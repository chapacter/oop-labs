package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

class ArrayTabulatedFunctionTest {

    @Test
    void testGetters() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};

        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        assertEquals(3, func.getCount());
        assertEquals(1.0, func.leftBound());
        assertEquals(3.0, func.rightBound());
        assertEquals(1.0, func.getX(0));
        assertEquals(4.0, func.getY(1));
    }

    @Test
    void testSetYAndIndexOf() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};

        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        func.setY(0, 10.0);
        assertEquals(10.0, func.getY(0));

        assertEquals(0, func.indexOfX(1.0));
        assertEquals(-1, func.indexOfX(10.0));

        assertEquals(0, func.indexOfY(10.0));
        assertEquals(-1, func.indexOfY(123.0));
    }

    @Test
    void testIllegalIndexes() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};

        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        assertThrows(IllegalArgumentException.class, () -> func.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> func.getX(3));

        assertThrows(IllegalArgumentException.class, () -> func.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> func.getY(3));

        assertThrows(IllegalArgumentException.class, () -> func.setY(5, 100));
    }

    @Test
    void testIteratorThrowsException() {
        double[] x = {1.0, 2.0};
        double[] y = {10.0, 20.0};

        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        assertThrows(UnsupportedOperationException.class, func::iterator);
    }

    @Test
    void testConstructorRejectsSmallTable() {
        double[] x = {1.0};
        double[] y = {2.0};
        assertThrows(IllegalArgumentException.class, () -> new ArrayTabulatedFunction(x, y));
    }

    @Test
    void testInterpolationAndExtrapolation() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        // Проверяем интерполяцию в середине
        double mid = func.interpolate(1.5, 0);
        assertEquals(3.0, mid, 1e-6);

        // Проверяем экстраполяцию слева
        double left = func.extrapolateLeft(0.0);
        assertTrue(left < 2.0);

        // Проверяем экстраполяцию справа
        double right = func.extrapolateRight(4.0);
        assertTrue(right > 6.0);
    }

    @Test
    void testFloorIndexOfXEdgeCases() {
        double[] x = {0.0, 5.0, 10.0};
        double[] y = {0.0, 25.0, 100.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        // Точно по точке
        assertEquals(0, func.floorIndexOfX(0.0));

        // Между точками
        assertEquals(1, func.floorIndexOfX(7.5));

        // Меньше левого края
        assertThrows(IllegalArgumentException.class, () -> func.floorIndexOfX(-5.0));

        // Правее правого края
        assertThrows(IllegalArgumentException.class, () -> func.floorIndexOfX(15.0));
    }

    @Test
    void testInsertAndRemove() {
        double[] x = {1.0, 3.0, 5.0};
        double[] y = {10.0, 30.0, 50.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        // Вставка нового элемента
        func.insert(4.0, 40.0);
        assertEquals(4.0, func.getX(func.indexOfY(40.0)));

        // Замена существующего
        func.insert(4.0, 44.0);
        assertEquals(44.0, func.getY(func.indexOfX(4.0)));

        // Удаление элемента
        int oldCount = func.getCount();
        func.remove(func.indexOfX(4.0));
        assertEquals(oldCount - 1, func.getCount());

        // Удаление несуществующего индекса
        assertThrows(IllegalArgumentException.class, () -> func.remove(100));
    }
}
