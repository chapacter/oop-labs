package ru.ssau.tk.avokado.lab2.operations;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {

    @Test
    public void testAsPointsNormalFunction() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Point[] points = TabulatedFunctionOperationService.asPoints(f);

        assertEquals(3, points.length);
        for (int i = 0; i < points.length; i++) {
            assertEquals(x[i], points[i].x, 1e-12);
            assertEquals(y[i], points[i].y, 1e-12);
        }
    }

    @Test
    public void testAsPointsSmallFunction() {
        double[] x = {0.0, 1.0};
        double[] y = {5.0, 10.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Point[] points = TabulatedFunctionOperationService.asPoints(f);
        assertEquals(2, points.length);
        assertEquals(0.0, points[0].x, 1e-12);
        assertEquals(10.0, points[1].y, 1e-12);
    }

    @Test
    public void testAsPointsNullThrows() {
        try {
            TabulatedFunctionOperationService.asPoints(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("TabulatedFunction is null", e.getMessage());
        }
    }
}
