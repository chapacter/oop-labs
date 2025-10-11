package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.exceptions.ArrayIsNotSortedException;
import ru.ssau.tk.avokado.lab2.exceptions.DifferentLengthOfArraysException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractTabulatedFunctionExceptionsTest {

    @Test
    public void checkLengthIsTheSameThrowsOnDifferentLengths() {
        final double[] x = {0.0, 1.0};
        final double[] y = {0.0};
        assertThrows(DifferentLengthOfArraysException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                AbstractTabulatedFunction.checkLengthIsTheSame(x, y);
            }
        });
    }

    @Test
    public void checkLengthIsTheSameThrowsOnNullArrays() {
        final double[] x = null;
        final double[] y = {0.0, 1.0};
        assertThrows(DifferentLengthOfArraysException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                AbstractTabulatedFunction.checkLengthIsTheSame(x, y);
            }
        });
    }

    @Test
    public void checkSortedThrowsOnNotStrictlyIncreasing() {
        final double[] bad = {0.0, 1.0, 1.0, 2.0};
        assertThrows(ArrayIsNotSortedException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                AbstractTabulatedFunction.checkSorted(bad);
            }
        });
    }

    @Test
    public void checkSortedThrowsOnNull() {
        final double[] bad = null;
        assertThrows(ArrayIsNotSortedException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                AbstractTabulatedFunction.checkSorted(bad);
            }
        });
    }
}
