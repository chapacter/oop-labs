package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.exceptions.ArrayIsNotSortedException;
import ru.ssau.tk.avokado.lab2.exceptions.DifferentLengthOfArraysException;
import ru.ssau.tk.avokado.lab2.exceptions.InterpolationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArrayTabulatedFunctionExceptionsTest {

    @Test
    public void constructorThrowsWhenLengthsDiffer() {
        final double[] x = {1.0, 2.0};
        final double[] y = {1.0};
        assertThrows(DifferentLengthOfArraysException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                new ArrayTabulatedFunction(x, y);
            }
        });
    }

    @Test
    public void constructorThrowsWhenXNotSorted() {
        final double[] x = {1.0, 3.0, 2.0};
        final double[] y = {2.0, 6.0, 4.0};
        assertThrows(ArrayIsNotSortedException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                new ArrayTabulatedFunction(x, y);
            }
        });
    }

    @Test
    public void interpolateProtectedThrowsInterpolationExceptionWhenXOutOfInterval() throws Exception {
        final double[] x = {0.0, 1.0, 2.0};
        final double[] y = {0.0, 1.0, 4.0};
        final ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        final Method m = f.getClass().getDeclaredMethod("interpolate", double.class, int.class);
        m.setAccessible(true);

        assertThrows(InterpolationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    m.invoke(f, 3.0, 1);
                } catch (InvocationTargetException ite) {
                    throw ite.getCause();
                }
            }
        });
    }
}
