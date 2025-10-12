package ru.ssau.tk.avokado.lab2.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.functions.MathFunction;
import ru.ssau.tk.avokado.lab2.functions.SqrFunction;

import static org.junit.jupiter.api.Assertions.*;

public class SteppingDifferentialOperatorsTest {

    private static final double EPS = 1e-12;

    @Test
    public void testInvalidStepsInConstructor() {
        try {
            new LeftSteppingDifferentialOperator(0.0);
            fail("Expected IllegalArgumentException for step = 0");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new RightSteppingDifferentialOperator(-1.0);
            fail("Expected IllegalArgumentException for negative step");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new MiddleSteppingDifferentialOperator(Double.POSITIVE_INFINITY);
            fail("Expected IllegalArgumentException for +Infinity");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            new LeftSteppingDifferentialOperator(Double.NaN);
            fail("Expected IllegalArgumentException for NaN");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testGetterSetterStep() {
        LeftSteppingDifferentialOperator op = new LeftSteppingDifferentialOperator(0.1);
        assertEquals(0.1, op.getStep(), EPS);

        op.setStep(0.2);
        assertEquals(0.2, op.getStep(), EPS);

        try {
            op.setStep(0.0);
            fail("Expected IllegalArgumentException for setting step = 0");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testLeftDifferenceOnSqrFunction() {
        MathFunction f = new SqrFunction(); // f(x) = x^2
        double h = 0.5;
        LeftSteppingDifferentialOperator left = new LeftSteppingDifferentialOperator(h);
        MathFunction df = left.derive(f);

        double x1 = 1.0;
        assertEquals(2.0 * x1 - h, df.apply(x1), EPS);

        double x2 = 0.0;
        assertEquals(2.0 * x2 - h, df.apply(x2), EPS);

        double x3 = 2.5;
        assertEquals(2.0 * x3 - h, df.apply(x3), EPS);
    }

    @Test
    public void testRightDifferenceOnSqrFunction() {
        MathFunction f = new SqrFunction();
        double h = 0.5;
        RightSteppingDifferentialOperator right = new RightSteppingDifferentialOperator(h);
        MathFunction df = right.derive(f);

        double x1 = 1.0;
        assertEquals(2.0 * x1 + h, df.apply(x1), EPS);

        double x2 = 0.0;
        assertEquals(2.0 * x2 + h, df.apply(x2), EPS);

        double x3 = 2.5;
        assertEquals(2.0 * x3 + h, df.apply(x3), EPS);
    }

    @Test
    public void testMiddleDifferenceOnSqrFunction() {
        MathFunction f = new SqrFunction();
        double h = 0.5;
        MiddleSteppingDifferentialOperator mid = new MiddleSteppingDifferentialOperator(h);
        MathFunction df = mid.derive(f);

        double x1 = 1.0;
        assertEquals(2.0 * x1, df.apply(x1), EPS);

        double x2 = 0.0;
        assertEquals(2.0 * x2, df.apply(x2), EPS);

        double x3 = 2.5;
        assertEquals(2.0 * x3, df.apply(x3), EPS);
    }

    @Test
    public void testDeriveNullThrows() {
        LeftSteppingDifferentialOperator left = new LeftSteppingDifferentialOperator(0.1);
        RightSteppingDifferentialOperator right = new RightSteppingDifferentialOperator(0.1);
        MiddleSteppingDifferentialOperator mid = new MiddleSteppingDifferentialOperator(0.1);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                left.derive(null);
            }
        });

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                right.derive(null);
            }
        });

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                mid.derive(null);
            }
        });
    }
}
