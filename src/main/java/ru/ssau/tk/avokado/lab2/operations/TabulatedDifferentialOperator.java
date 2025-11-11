package ru.ssau.tk.avokado.lab2.operations;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.functions.factory.TabulatedFunctionFactory;
import ru.ssau.tk.avokado.lab2.concurrent.SynchronizedTabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedDifferentialOperator.class);

    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
        logger.debug("TabulatedDifferentialOperator создан с фабрикой ArrayTabulatedFunctionFactory");
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
        logger.debug("TabulatedDifferentialOperator создан с фабрикой {}", factory.getClass().getSimpleName());
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
        logger.debug("derive: вычисление производной для функции с {} точками", count);
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
        logger.info("derive: производная успешно рассчитана");
        return factory.create(xValues, yValues);
    }

    public TabulatedFunction deriveSynchronously(final TabulatedFunction function) {
        if (function == null) {
            logger.error("deriveSynchronously: function == null");
            throw new IllegalArgumentException("function is null");
        }
        logger.info("deriveSynchronously: запуск (обёртка в SynchronizedTabulatedFunction при необходимости)");

        final SynchronizedTabulatedFunction wrapper;
        if (function instanceof SynchronizedTabulatedFunction) {
            wrapper = (SynchronizedTabulatedFunction) function;
            logger.debug("deriveSynchronously: функция уже синхронизирована");
        } else {
            wrapper = new SynchronizedTabulatedFunction(function);
            logger.debug("deriveSynchronously: создана синхронизированная обёртка");
        }
        logger.info("deriveSynchronously: завершено");
        return wrapper.doSynchronously(new SynchronizedTabulatedFunction.Operation<TabulatedFunction>() {

            @Override
            public TabulatedFunction apply(SynchronizedTabulatedFunction synchronizedFunction) {
                logger.debug("deriveSynchronously: внутри doSynchronously — делегирование в derive()");
                return TabulatedDifferentialOperator.this.derive(synchronizedFunction);

            }
        });
    }

}