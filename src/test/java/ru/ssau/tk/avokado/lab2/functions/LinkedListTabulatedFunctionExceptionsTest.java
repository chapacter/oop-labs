package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.exceptions.ArrayIsNotSortedException;
import ru.ssau.tk.avokado.lab2.exceptions.DifferentLengthOfArraysException;
import ru.ssau.tk.avokado.lab2.exceptions.InterpolationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinkedListTabulatedFunctionExceptionsTest {

    @Test
    public void constructorThrowsWhenLengthsDiffer() {
        final double[] x = {0.0, 1.0};
        final double[] y = {0.0};
        assertThrows(DifferentLengthOfArraysException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                new LinkedListTabulatedFunction(x, y);
            }
        });
    }

    @Test
    public void constructorThrowsWhenXNotSorted() {
        final double[] x = {0.0, 2.0, 1.0};
        final double[] y = {0.0, 4.0, 1.0};
        assertThrows(ArrayIsNotSortedException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                new LinkedListTabulatedFunction(x, y);
            }
        });
    }

    @Test
    public void interpolateProtectedThrowsInterpolationExceptionWhenXOutOfInterval() throws Exception {
        final double[] x = {0.0, 1.0, 2.0};
        final double[] y = {0.0, 1.0, 4.0};
        final LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        final Method m = f.getClass().getDeclaredMethod("interpolate", double.class, int.class);
        m.setAccessible(true);

        assertThrows(InterpolationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    m.invoke(f, -1.0, 0);
                } catch (InvocationTargetException ite) {
                    throw ite.getCause();
                }
            }
        });
    }
}
