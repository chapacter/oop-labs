package ru.ssau.tk.avokado.lab2.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;
import ru.ssau.tk.avokado.lab2.operations.TabulatedFunctionOperationService;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedTabulatedFunction.class);
    private final TabulatedFunction delegate;
    private final Object mutex;

    public SynchronizedTabulatedFunction(TabulatedFunction delegate) {
        this(delegate, null);
    }

    public SynchronizedTabulatedFunction(TabulatedFunction delegate, Object mutex) {
        if (delegate == null) {
            logger.error("SynchronizedTabulatedFunction: delegate == null (нельзя создать синхронизированную обёртку для null)");
            throw new IllegalArgumentException("delegate is null");
        }
        this.delegate = delegate;
        this.mutex = (mutex == null) ? this : mutex;
        logger.info("Создан SynchronizedTabulatedFunction для delegate типа {}", delegate.getClass().getSimpleName());
    }

    @Override
    public int getCount() {
        synchronized (mutex) {
            return delegate.getCount();
        }
    }

    @Override
    public double getX(int index) {
        synchronized (mutex) {
            return delegate.getX(index);
        }
    }

    @Override
    public double getY(int index) {
        synchronized (mutex) {
            return delegate.getY(index);
        }
    }

    @Override
    public void setY(int index, double value) {
        synchronized (mutex) {
            delegate.setY(index, value);
        }
    }

    @Override
    public int indexOfX(double x) {
        synchronized (mutex) {
            return delegate.indexOfX(x);
        }
    }

    @Override
    public int indexOfY(double y) {
        synchronized (mutex) {
            return delegate.indexOfY(y);
        }
    }

    @Override
    public double leftBound() {
        synchronized (mutex) {
            return delegate.leftBound();
        }
    }

    @Override
    public double rightBound() {
        synchronized (mutex) {
            return delegate.rightBound();
        }
    }

    @Override
    public double apply(double x) {
        synchronized (mutex) {
            return delegate.apply(x);
        }
    }

    @Override
    public Iterator<Point> iterator() {
        final Point[] it;
        synchronized (mutex) {
            logger.debug("iterator(): создание итератора делегата внутри синхронизированного блока");
            it = TabulatedFunctionOperationService.asPoints(delegate);
        }
        return new Iterator<Point>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                synchronized (mutex) {
                    logger.trace("iterator.hasNext -> {}", index < it.length);
                    return index < it.length;
                }
            }

            @Override
            public Point next() {
                synchronized (mutex) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return it[index++];
                }
            }

            @Override
            public void remove() {
                synchronized (mutex) {
                    throw new UnsupportedOperationException("remove");
                }
            }
        };
    }

    @Override
    public String toString() {
        synchronized (mutex) {
            return delegate.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SynchronizedTabulatedFunction that)) return false;
        synchronized (mutex) {
            return Objects.equals(delegate, that.delegate);
        }
    }

    @Override
    public int hashCode() {
        synchronized (mutex) {
            return Objects.hashCode(delegate);
        }
    }

    public <T> T doSynchronously(Operation<? extends T> operation) {
        if (operation == null) {
            logger.error("doSynchronously: operation == null");
            throw new IllegalArgumentException("operation is null");
        }
        logger.debug("doSynchronously: начинаем выполнение операции {}, синхронизированно", operation.getClass().getName());
        synchronized (mutex) {
            return operation.apply(this);
        }
    }

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction function);
    }
}
