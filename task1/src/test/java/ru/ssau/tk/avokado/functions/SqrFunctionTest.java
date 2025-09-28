package ru.ssau.tk.avokado.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqrFunctionTest {

    private final MathFunction sqr = new SqrFunction();


    @Test
    void testZero() {
        assertEquals(0.0, sqr.apply(0.0), 1e-9);
    }

    @Test
    void testPositiveInteger() {
        assertEquals(4.0, sqr.apply(2.0),1e-9);
        assertEquals(81.0, sqr.apply(9.0),1e-9);
    }

    @Test
    void testNegativeInteger() {
        assertEquals(25.0, sqr.apply(-5.0),1e-9);
        assertEquals(144.0, sqr.apply(-12.0),1e-9);
    }

    @Test
    void testPositiveFractional() {
        assertEquals(0.25, sqr.apply(0.5),1e-9);
    }

    @Test
    void testNegativeFractional() {
        assertEquals(0.49, sqr.apply(-0.7),1e-9);
    }

    @Test
    void testLargeValue() {
        double x = 1e7;
        assertEquals(1e14, sqr.apply(x),1e-9);
    }

    @Test
    void testNaN() {
        assertTrue(Double.isNaN(sqr.apply(Double.NaN)));
    }

    @Test
    void testInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, sqr.apply(Double.POSITIVE_INFINITY),1e-9);
        assertEquals(Double.POSITIVE_INFINITY, sqr.apply(Double.NEGATIVE_INFINITY),1e-9);
    }
}
