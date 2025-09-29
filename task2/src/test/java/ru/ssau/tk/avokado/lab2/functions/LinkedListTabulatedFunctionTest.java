package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionTest {

    @Test
    void testConstructorWithArrays() {
        double[] x = {0, 1, 2};
        double[] y = {0, 2, 4};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(3, f.getCount());
        assertEquals(0, f.getX(0));
        assertEquals(2, f.getY(1));
    }

    @Test
    void testConstructorWithFunction() {
        MathFunction sqr = new SqrFunction();
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(sqr, 0, 2, 3);

        assertEquals(3, f.getCount());
        assertEquals(0.0, f.getX(0));
        assertEquals(1.0, f.getY(1)); // 1^2 = 1
        assertEquals(4.0, f.getY(2)); // 2^2 = 4
    }

    @Test
    void testGetSetY() {
        double[] x = {0, 1};
        double[] y = {10, 20};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(10, f.getY(0));
        f.setY(0, 15);
        assertEquals(15, f.getY(0));
    }

    @Test
    void testIndexOfXAndY() {
        double[] x = {0, 1, 2};
        double[] y = {5, 10, 15};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(1, f.indexOfX(1));
        assertEquals(-1, f.indexOfX(10));
        assertEquals(2, f.indexOfY(15));
        assertEquals(-1, f.indexOfY(100));
    }

    @Test
    void testBounds() {
        double[] x = {0, 1, 2};
        double[] y = {5, 10, 15};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(0, f.leftBound());
        assertEquals(2, f.rightBound());
    }

    @Test
    void testApplyInsideInterval() {
        double[] x = {0, 1, 2};
        double[] y = {0, 2, 4};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        // Интерполяция в середине
        assertEquals(1.0, f.apply(0.5), 1e-9);
        assertEquals(3.0, f.apply(1.5), 1e-9);
    }

    @Test
    void testApplyOutsideInterval() {
        double[] x = {0, 1, 2};
        double[] y = {0, 2, 4};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        // Экстраполяция на краях
        assertEquals(-2.0, f.apply(-1));
        assertEquals(6.0, f.apply(3));
    }

    @Test
    void testSinglePointFunction() {
        double[] x = {1};
        double[] y = {10};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(10, f.apply(1));
    }

    @Test
public void testApply() {
    double[] xValues = {0.0, 1.0, 2.0};
    double[] yValues = {0.0, 1.0, 4.0};
    LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

    // Точное совпадение
    assertEquals(0.0, function.apply(0.0), 1e-12);
    assertEquals(1.0, function.apply(1.0), 1e-12);

    // Интерполяция
    assertEquals(0.5, function.apply(0.5), 1e-12);
    assertEquals(2.5, function.apply(1.5), 1e-12);

    // Экстраполяция
    assertEquals(-1.0, function.apply(-1.0), 1e-12); // left
    assertEquals(7.0, function.apply(3.0), 1e-12);   // right
}
}

