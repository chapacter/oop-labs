package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeBoorFunctionTest {
    @Test
    void testSimpleSplineValue() {
        double[] nodes = {0, 0, 0, 1, 2, 3, 3, 3};
        double[] controlPoints = {0, 1, 0, 1, 0};
        int degree = 2;

        MathFunction spline = new DeBoorFunction(nodes, controlPoints, degree);

        double result = spline.apply(1.5);
        assertEquals(0.25, result, 1e-9);
    }

    @Test
    void testLeftBoundary() {
        double[] nodes = {0, 0, 0, 1, 2, 3, 3, 3};
        double[] controlPoints = {0, 1, 0, 1, 0};
        int degree = 2;

        MathFunction spline = new DeBoorFunction(nodes, controlPoints, degree);

        double result = spline.apply(0);
        assertEquals(0, result, 1e-9);
    }

    @Test
    void testRightBoundary() {
        double[] nodes = {0, 0, 0, 1, 2, 3, 3, 3};
        double[] controlPoints = {0, 1, 0, 1, 0};
        int degree = 2;

        MathFunction spline = new DeBoorFunction(nodes, controlPoints, degree);

        double result = spline.apply(3);
        // В правой границе должен совпадать с последней контрольной точкой
        assertEquals(0, result, 1e-9);
    }

    @Test
    void testMiddlePoint() {
        double[] nodes = {0, 0, 0, 1, 2, 3, 3, 3};
        double[] controlPoints = {0, 1, 0, 1, 0};
        int degree = 2;

        MathFunction spline = new DeBoorFunction(nodes, controlPoints, degree);

        double result = spline.apply(2.0);
        assertTrue(result >= 0 && result <= 1, "Значение должно быть между контрольными точками");
    }

    @Test
    void testInvalidConstructorThrowsException() {
        double[] nodes = {0, 1, 2}; // слишком мало узлов
        double[] controlPoints = {1, 2, 3};
        int degree = 2;

        assertThrows(IllegalArgumentException.class, () ->
                new DeBoorFunction(nodes, controlPoints, degree));
    }

    @Test
    void testLinearSplineDegree1() {
        double[] nodes = {0, 0, 1, 2, 2};
        double[] controlPoints = {0, 2, 4};
        int degree = 1;

        MathFunction spline = new DeBoorFunction(nodes, controlPoints, degree);

        assertEquals(1.0, spline.apply(0.5), 1e-9); // линейная интерполяция между (0,0) и (1,2)
        assertEquals(3.0, spline.apply(1.5), 1e-9); // линейная интерполяция между (1,2) и (2,4)
    }
}