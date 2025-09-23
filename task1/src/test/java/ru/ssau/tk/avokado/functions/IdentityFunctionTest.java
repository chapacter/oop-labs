package ru.ssau.tk.avokado.functions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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