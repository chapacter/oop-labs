package ru.ssau.tk.avokado.lab2.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.exceptions.InconsistentFunctionsException;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;

public class TabulatedFunctionOperationService {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionOperationService.class);

    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory is null");
        }
        this.factory = factory;
        logger.debug("Создан TabulatedFunctionOperationService с пользовательской фабрикой {}", factory.getClass().getSimpleName());
    }

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("TabulatedFunction is null");
        }

        int count = tabulatedFunction.getCount();
        logger.debug("Преобразование функции в массив точек, count={}", count);
        Point[] points = new Point[count];

        int i = 0;
        for (Point point : tabulatedFunction) {
            points[i++] = new Point(point.x(), point.y());
        }
        logger.debug("asPoints: преобразовано {} точек", count);
        return points;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory is null");
        }
        this.factory = factory;
        logger.info("Фабрика установлена: {}", factory.getClass().getSimpleName());
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        if (a == null || b == null) {
            logger.error("doOperation: один из аргументов == null");
            throw new IllegalArgumentException("One of functions is null");
        }

        int n = a.getCount();
        if (n != b.getCount()) {
            logger.warn("doOperation: разные количества точек: a={} b={}", n, b.getCount());
            throw new InconsistentFunctionsException("Different number of points");
        }

        Point[] pa = asPoints(a);
        Point[] pb = asPoints(b);

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            double xa = pa[i].x();
            double xb = pb[i].x();
            if (Double.compare(xa, xb) != 0) {
                logger.warn("doOperation: несоответствие x на позиции {}: {} != {}", i, pa[i].x(), pb[i].x());
                throw new InconsistentFunctionsException(
                        "x-values differ at index " + i + ": " + xa + " vs " + xb);
            }
            xValues[i] = xa;
            yValues[i] = operation.apply(pa[i].y(), pb[i].y());
        }
        logger.info("doOperation: операция успешно применена к {} точкам", n);
        return factory.create(xValues, yValues);
    }

    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        logger.info("add: складываем функции");
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u + v;
            }
        });
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        logger.info("subtract: вычитаем функции");
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u - v;
            }
        });
    }

    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        logger.info("multiply: умножаем функции");
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u * v;
            }
        });
    }

    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        logger.info("divide: делим функции");
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                if (v == 0) {
                    throw new ArithmeticException("Division by zero at some point");
                }
                return u / v;
            }
        });
    }

    private interface BiOperation {
        double apply(double u, double v);
    }
}
