package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;

class ArrayTabulatedFunctionTest {

    @Test
    void testGetters() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};

        ArrayTabulatedFunction function= new ArrayTabulatedFunction(x, y);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.leftBound());
        assertEquals(3.0, function.rightBound());
        assertEquals(1.0, function.getX(0));
        assertEquals(4.0, function.getY(1));
    }

    @Test
    void testSetYAndIndexOf() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};

        ArrayTabulatedFunction function= new ArrayTabulatedFunction(x, y);

        function.setY(0, 10.0);
        assertEquals(10.0, function.getY(0));

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(-1, function.indexOfX(10.0));

        assertEquals(0, function.indexOfY(10.0));
        assertEquals(-1, function.indexOfY(123.0));
    }

    @Test
    void testIllegalIndexes() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0};

        ArrayTabulatedFunction function= new ArrayTabulatedFunction(x, y);

        assertThrows(IllegalArgumentException.class, () -> function.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getX(3));

        assertThrows(IllegalArgumentException.class, () -> function.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getY(3));

        assertThrows(IllegalArgumentException.class, () -> function.setY(5, 100));
    }

//    @Test
//    void testIteratorThrowsException() {
//        double[] x = {1.0, 2.0};
//        double[] y = {10.0, 20.0};
//
//        ArrayTabulatedFunction function= new ArrayTabulatedFunction(x, y);
//
//        assertThrows(UnsupportedOperationException.class, function::iterator);
//    }

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
        ArrayTabulatedFunction function= new ArrayTabulatedFunction(x, y);

        // Проверяем интерполяцию в середине
        double mid = function.interpolate(1.5, 0);
        assertEquals(3.0, mid, 1e-6);

        // Проверяем экстраполяцию слева
        double left = function.extrapolateLeft(0.0);
        assertTrue(left < 2.0);

        // Проверяем экстраполяцию справа
        double right = function.extrapolateRight(4.0);
        assertTrue(right > 6.0);
    }

    @Test
    void testFloorIndexOfXEdgeCases() {
        double[] x = {0.0, 5.0, 10.0};
        double[] y = {0.0, 25.0, 100.0};
        ArrayTabulatedFunction function= new ArrayTabulatedFunction(x, y);

        // Точно по точке
        assertEquals(0, function.floorIndexOfX(0.0));

        // Между точками
        assertEquals(1, function.floorIndexOfX(7.5));

        // Меньше левого края
        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(-5.0));

        // Правее правого края
        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(15.0));
    }

    @Test
    void testInsertAndRemove() {
        double[] x = {1.0, 3.0, 5.0};
        double[] y = {10.0, 30.0, 50.0};
        ArrayTabulatedFunction function= new ArrayTabulatedFunction(x, y);

        // Вставка нового элемента
        function.insert(4.0, 40.0);
        assertEquals(4.0, function.getX(function.indexOfY(40.0)));

        // Замена существующего
        function.insert(4.0, 44.0);
        assertEquals(44.0, function.getY(function.indexOfX(4.0)));

        // Удаление элемента
        int oldCount = function.getCount();
        function.remove(function.indexOfX(4.0));
        assertEquals(oldCount - 1, function.getCount());

        // Удаление несуществующего индекса
        assertThrows(IllegalArgumentException.class, () -> function.remove(100));
    }
}
