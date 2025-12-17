package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeFunctionTest {

    @Test
    void testIdentityThenSqr() {
        MathFunction f = new IdentityFunction();
        MathFunction g = new SqrFunction();

        MathFunction composite = new CompositeFunction(f, g);

        assertEquals(9.0, composite.apply(3.0), 1e-9); // g(f(3)) = (3)^2 = 9
    }

    @Test
    void testSqrThenIdentity() {
        MathFunction f = new SqrFunction();
        MathFunction g = new IdentityFunction();

        MathFunction composite = new CompositeFunction(f, g);

        assertEquals(16.0, composite.apply(4.0), 1e-9); // g(f(4)) = 16
    }

    @Test
    void testCompositeOfComposite() {
        MathFunction identity = new IdentityFunction();
        MathFunction sqr = new SqrFunction();

        // h(x) = (x^2)^2 = x^4
        MathFunction composite = new CompositeFunction(
                new CompositeFunction(identity, sqr),
                sqr
        );

        assertEquals(81.0, composite.apply(3.0), 1e-9); // (3^2)^2 = 81
    }

    @Test
    void testCompositeWithSameFunctions() {
        MathFunction sqr = new SqrFunction();
        MathFunction composite = new CompositeFunction(sqr, sqr);
        // h(x) = sqr(sqr(x)) = (x^2)^2

        assertEquals(16.0, composite.apply(2.0), 1e-9); // (2^2)^2 = 16
    }
}