package ru.ssau.tk.avokado.lab2.operations;

import ru.ssau.tk.avokado.lab2.exceptions.InconsistentFunctionsException;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;

public class TabulatedFunctionOperationService {

    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory is null");
        }
        this.factory = factory;
    }

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory is null");
        }
        this.factory = factory;
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("TabulatedFunction is null");
        }

        int count = tabulatedFunction.getCount();
        Point[] points = new Point[count];

        int i = 0;
        for (Point point : tabulatedFunction) {
            points[i++] = new Point(point.x, point.y);
        }

        return points;
    }

    private interface BiOperation {
        double apply(double u, double v);
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("One of functions is null");
        }

        int n = a.getCount();
        if (n != b.getCount()) {
            throw new InconsistentFunctionsException("Different number of points");
        }

        Point[] pa = asPoints(a);
        Point[] pb = asPoints(b);

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            double xa = pa[i].x;
            double xb = pb[i].x;
            if (Double.compare(xa, xb) != 0) {
                throw new InconsistentFunctionsException(
                        "x-values differ at index " + i + ": " + xa + " vs " + xb);
            }
            xValues[i] = xa;
            yValues[i] = operation.apply(pa[i].y, pb[i].y);
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u + v;
            }
        });
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u - v;
            }
        });
    }
}
