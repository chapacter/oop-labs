package ru.ssau.tk.avokado.lab2.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.ssau.tk.avokado.lab2.exceptions.InconsistentFunctionsException;
import ru.ssau.tk.avokado.lab2.functions.*;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.LinkedListTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionOperationServiceTest {

    @Test
    public void testDefaultConstructorSetsArrayFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    public void testCtorWithFactoryAndSetter() {
        TabulatedFunctionFactory lf = new LinkedListTabulatedFunctionFactory();
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(lf);
        assertTrue(service.getFactory() instanceof LinkedListTabulatedFunctionFactory);

        service.setFactory(new ArrayTabulatedFunctionFactory());
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    public void testAsPointsWorks() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Point[] pts = TabulatedFunctionOperationService.asPoints(f);
        assertEquals(3, pts.length);
        assertEquals(1.0, pts[0].x, 1e-12);
        assertEquals(20.0, pts[1].y, 1e-12);
    }

    @Test
    public void testAsPointsNormalFunction() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Point[] points = TabulatedFunctionOperationService.asPoints(f);

        assertEquals(3, points.length);
        for (int i = 0; i < points.length; i++) {
            assertEquals(x[i], points[i].x, 1e-12);
            assertEquals(y[i], points[i].y, 1e-12);
        }
    }

    @Test
    public void testAsPointsSmallFunction() {
        double[] x = {0.0, 1.0};
        double[] y = {5.0, 10.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Point[] points = TabulatedFunctionOperationService.asPoints(f);
        assertEquals(2, points.length);
        assertEquals(0.0, points[0].x, 1e-12);
        assertEquals(10.0, points[1].y, 1e-12);
    }

    @Test
    public void testAsPointsNullThrows() {
        try {
            TabulatedFunctionOperationService.asPoints(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("TabulatedFunction is null", e.getMessage());
        }
    }

    @Test
    public void testAddTwoArrayFunctions() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        double[] x = {0.0, 1.0, 2.0};
        double[] y1 = {0.0, 1.0, 4.0};
        double[] y2 = {1.0, 2.0, 3.0};

        TabulatedFunction a = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x, y2);

        TabulatedFunction res = service.add(a, b);

        assertEquals(3, res.getCount());
        assertEquals(1.0, res.getY(0), 1e-12);
        assertEquals(3.0, res.getY(1), 1e-12);
        assertEquals(7.0, res.getY(2), 1e-12);
        assertTrue(res instanceof ArrayTabulatedFunction);
    }

    @Test
    public void testSubtractDifferentTypesAndFactorySwitch() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        double[] x = {0.0, 1.0, 2.0};
        double[] yA = {5.0, 6.0, 7.0};
        double[] yB = {1.0, 2.0, 3.0};

        TabulatedFunction a = new ArrayTabulatedFunction(x, yA);
        TabulatedFunction b = new LinkedListTabulatedFunction(x, yB);

        TabulatedFunction res = service.subtract(a, b);

        assertEquals(3, res.getCount());
        assertEquals(4.0, res.getY(0), 1e-12);
        assertEquals(4.0, res.getY(1), 1e-12);
        assertEquals(4.0, res.getY(2), 1e-12);

        // переключаем фабрику на LinkedList и проверяем, что результат создаётся через текущую фабрику
        service.setFactory(new LinkedListTabulatedFunctionFactory());
        TabulatedFunction res2 = service.add(a, b);
        assertTrue(res2 instanceof LinkedListTabulatedFunction);
    }

    @Test
    public void testDifferentCountsThrows() {
        final TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        final TabulatedFunction a = new ArrayTabulatedFunction(new double[]{0,1}, new double[]{0,1});
        final TabulatedFunction b = new ArrayTabulatedFunction(new double[]{0,1,2}, new double[]{0,1,4});

        assertThrows(InconsistentFunctionsException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.add(a, b);
            }
        });
    }

    @Test
    public void testDifferentXValuesThrows() {
        final TabulatedFunctionOperationService service =
                new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        final TabulatedFunction a = new ArrayTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );
        // исправлено: массив x во второй функции тоже строго возрастающий, но отличается в позиции 1
        final TabulatedFunction b = new ArrayTabulatedFunction(
                new double[]{0.0, 1.5, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );

        assertThrows(InconsistentFunctionsException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                service.subtract(a, b);
            }
        });
    }


    @Test
    public void testNullArgumentsThrowIllegalArgument() {
        final TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.add(null, new ArrayTabulatedFunction(new double[]{0,1}, new double[]{0,1}));
            }
        });

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                service.subtract(new ArrayTabulatedFunction(new double[]{0,1}, new double[]{0,1}), null);
            }
        });
    }
}

