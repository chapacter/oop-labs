package ru.ssau.tk.avokado.lab2.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.concurrent.SynchronizedTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.SqrFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.UnitFunction;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedDifferentialOperatorSynchronouslyTest {

    @Test
    void testDeriveSynchronouslyOnArrayFunctionMatchesDerive() {
        TabulatedFunction f = new ArrayTabulatedFunction(new SqrFunction(), 0.0, 2.0, 5);
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();

        TabulatedFunction dPlain = op.derive(f);
        TabulatedFunction dSync = op.deriveSynchronously(f);

        assertNotNull(dPlain);
        assertNotNull(dSync);
        assertEquals(dPlain.getCount(), dSync.getCount());

        for (int i = 0; i < dPlain.getCount(); i++) {
            assertEquals(dPlain.getX(i), dSync.getX(i), 1e-12, "X mismatch at index " + i);
            assertEquals(dPlain.getY(i), dSync.getY(i), 1e-9, "Y mismatch at index " + i);
        }
    }

    @Test
    void testDeriveSynchronouslyOnAlreadySynchronizedFunction() {
        TabulatedFunction base = new ArrayTabulatedFunction(new UnitFunction(), 1.0, 2.0, 5);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();

        TabulatedFunction dPlain = op.derive(base);
        TabulatedFunction dSync = op.deriveSynchronously(sync);

        assertEquals(dPlain.getCount(), dSync.getCount());
        for (int i = 0; i < dPlain.getCount(); i++) {
            assertEquals(dPlain.getX(i), dSync.getX(i), 1e-12);
            assertEquals(dPlain.getY(i), dSync.getY(i), 1e-12);
        }
    }

    @Test
    void testDeriveSynchronouslyNullThrows() {
        final TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        Executable exec = new Executable() {
            @Override
            public void execute() throws Throwable {
                op.deriveSynchronously(null);
            }
        };
        assertThrows(IllegalArgumentException.class, exec);
    }
}
