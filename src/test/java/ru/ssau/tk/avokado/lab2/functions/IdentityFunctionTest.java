package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityFunctionTest {

    private final IdentityFunction identityFunction = new IdentityFunction();

    @Test
    void testPositiveNumber() {
        assertEquals(5.0, identityFunction.apply(5.0));
    }

    @Test
    void testNegativeNumber() {
        assertEquals(-3.14, identityFunction.apply(-3.14));
    }

    @Test
    void testZero() {
        assertEquals(0.0, identityFunction.apply(0.0));
    }

    @Test
    void testInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, identityFunction.apply(Double.POSITIVE_INFINITY));
        assertEquals(Double.NEGATIVE_INFINITY, identityFunction.apply(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testNaN() {
        assertTrue(Double.isNaN(identityFunction.apply(Double.NaN)));
    }
}