package ru.ssau.tk.avokado.lab2.concurrent;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizedTabulatedFunctionDoSynchronouslyTest {

    @Test
    void testDoSynchronouslyReturnsDoubleSum() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {1.0, 2.0, 3.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        SynchronizedTabulatedFunction.Operation<Double> sumOp = new SynchronizedTabulatedFunction.Operation<Double>() {
            @Override
            public Double apply(SynchronizedTabulatedFunction function) {
                double sum = 0.0;
                int n = function.getCount();
                for (int i = 0; i < n; i++) {
                    sum += function.getY(i);
                }
                return sum;
            }
        };

        Double result = sync.doSynchronously(sumOp);
        assertEquals(6.0, result, 1e-12);
    }

    @Test
    void testDoSynchronouslyVoidOperationModifiesUnderlying() {
        double[] x = {0.0, 1.0};
        double[] y = {5.0, 7.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        SynchronizedTabulatedFunction.Operation<Void> op = new SynchronizedTabulatedFunction.Operation<Void>() {
            @Override
            public Void apply(SynchronizedTabulatedFunction function) {
                function.setY(0, 123.0);
                return null; // Void -> возвращаем null
            }
        };

        Void r = sync.doSynchronously(op);
        assertNull(r);
        assertEquals(123.0, base.getY(0), 1e-12);
    }

    @Test
    void testDoSynchronouslyReturnsStringFromDelegateToString() {
        double[] x = {1.0, 2.0};
        double[] y = {2.0, 3.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        SynchronizedTabulatedFunction.Operation<String> op = new SynchronizedTabulatedFunction.Operation<String>() {
            @Override
            public String apply(SynchronizedTabulatedFunction function) {
                return function.toString();
            }
        };

        String s = sync.doSynchronously(op);
        assertNotNull(s);
        assertTrue(s.length() > 0);
    }

    @Test
    void testDoSynchronouslyNullOperationThrows() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        try {
            sync.doSynchronously(null);
            fail("Expected IllegalArgumentException for null operation");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    void testDoSynchronouslyCanUseGetCountAndApply() {
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {1.0, 1.0, 1.0, 1.0};
        ArrayTabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        SynchronizedTabulatedFunction.Operation<Integer> op = new SynchronizedTabulatedFunction.Operation<Integer>() {
            @Override
            public Integer apply(SynchronizedTabulatedFunction function) {
                int cnt = function.getCount();
                double sum = 0;
                for (int i = 0; i < cnt; i++) {
                    sum += function.apply(function.getX(i));
                }
                return (int) Math.round(sum);
            }
        };

        Integer result = sync.doSynchronously(op);
        assertEquals(4, result.intValue());
    }
}
