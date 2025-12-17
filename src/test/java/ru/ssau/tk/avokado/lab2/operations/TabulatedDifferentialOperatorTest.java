package ru.ssau.tk.avokado.lab2.operations;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.LinkedListTabulatedFunctionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TabulatedDifferentialOperatorTest {

    @Test
    void testDeriveLinearFunction_ArrayFactory() {
        double[] x = {0, 1, 2, 3};
        double[] y = {0, 2, 4, 6}; // y = 2x → производная = 2
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory());
        TabulatedFunction df = op.derive(f);

        for (int i = 0; i < df.getCount(); i++) {
            assertEquals(2.0, df.getY(i), 1e-9);
        }
    }

    @Test
    void testDeriveQuadraticFunction_LinkedListFactory() {
        double[] x = {0, 1, 2, 3};
        double[] y = {0, 1, 4, 9};
        TabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());
        TabulatedFunction df = op.derive(f);

        for (int i = 0; i < df.getCount(); i++) {
            assertEquals(2 * x[i], df.getY(i), 1.0);
        }
    }

    @Test
    void testFactoryGetterSetter() {
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        assertInstanceOf(ArrayTabulatedFunctionFactory.class, op.getFactory());

        op.setFactory(new LinkedListTabulatedFunctionFactory());
        assertInstanceOf(LinkedListTabulatedFunctionFactory.class, op.getFactory());
    }
}