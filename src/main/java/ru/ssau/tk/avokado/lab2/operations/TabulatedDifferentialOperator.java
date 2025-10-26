package ru.ssau.tk.avokado.lab2.operations;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.concurrent.SynchronizedTabulatedFunction;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {

    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        int count = function.getCount();
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = function.getX(i);
            if (i == 0) {
                yValues[i] = (function.getY(i + 1) - function.getY(i))
                        / (function.getX(i + 1) - function.getX(i));
            } else if (i == count - 1) {
                yValues[i] = (function.getY(i) - function.getY(i - 1)) / (function.getX(i) - function.getX(i - 1));
            } else {
                yValues[i] = (function.getY(i + 1) - function.getY(i - 1)) / (function.getX(i + 1) - function.getX(i - 1));
            }
        }
        return factory.create(xValues, yValues);
    }

    public TabulatedFunction deriveSynchronously(final TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("function is null");
        }

        final SynchronizedTabulatedFunction wrapper;
        if (function instanceof SynchronizedTabulatedFunction) {
            wrapper = (SynchronizedTabulatedFunction) function;
        } else {
            wrapper = new SynchronizedTabulatedFunction(function);
        }

        return wrapper.doSynchronously(new SynchronizedTabulatedFunction.Operation<TabulatedFunction>() {
            @Override
            public TabulatedFunction apply(SynchronizedTabulatedFunction synchronizedFunction) {
                return TabulatedDifferentialOperator.this.derive(synchronizedFunction);
            }
        });
    }

}