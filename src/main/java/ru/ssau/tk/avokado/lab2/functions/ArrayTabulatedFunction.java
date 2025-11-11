package ru.ssau.tk.avokado.lab2.functions;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import ru.ssau.tk.avokado.lab2.exceptions.InterpolationException;
import ru.ssau.tk.avokado.lab2.exceptions.DifferentLengthOfArraysException;
import ru.ssau.tk.avokado.lab2.exceptions.ArrayIsNotSortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Прописываем класс ArrayTabulatedFunction для расширения класса AbstractTabulatedFunction
public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {

    @Serial
    private static final long serialVersionUID = 917223344556677889L;
    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunction.class);

    private double[] xValues;
    private double[] yValues;
    private int count;

    // Прописываем первый конструктор, состоящий из массивов x и y
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        if (xValues.length < 2) {
            logger.error("Конструктор ArrayTabulatedFunction: длина таблицы < 2");
            throw new IllegalArgumentException("Длина таблицы < 2");
        }
        AbstractTabulatedFunction.checkSorted(xValues);

        // Создаем копии массивов методом Arrays.copyOf()
        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);
        this.count = xValues.length;
        logger.info("ArrayTabulatedFunction создана с {} точками", this.count);
    }

    // Прописываем второй конструктор для дискретизации функции
    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (source == null) {
            logger.error("ArrayTabulatedFunction: source == null");
            throw new IllegalArgumentException("функция = null");
        }
        if (count < 2) {
            logger.error("ArrayTabulatedFunction: count < 2");
            throw new IllegalArgumentException("Кол-во точек < 2");
        }
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        this.count = count;
        this.xValues = new double[count];
        this.yValues = new double[count];

        if (xFrom == xTo) {
            Arrays.fill(xValues, xFrom);
            double yValue = source.apply(xFrom);
            Arrays.fill(yValues, yValue);
        } else {
            // Проводим дискретизацию
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                xValues[i] = xFrom + i * step;
                yValues[i] = source.apply(xValues[i]);
            }
        }
        logger.info("ArrayTabulatedFunction создана: [{}..{}], count={}", xFrom, xTo, count);
    }

    // Прописываем реализацию методов для TabulatedFunction
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) {
            logger.error("getX: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            logger.error("getY: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            logger.error("setY: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(xValues[i] - x) < 1e-12) {
                logger.trace("indexOfX: найдено x={} на индексе {}", x, i);
                return i;
            }
        }
        logger.trace("indexOfX: x={} не найден", x);
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(yValues[i] - y) < 1e-12) {
                logger.trace("indexOfY: найдено y={} на индексе {}", y, i);
                return i;
            }
        }
        logger.trace("indexOfY: y={} не найден", y);
        return -1;
    }

    @Override
    public double leftBound() {
        logger.trace("leftBound: {}", xValues[0] );
        return xValues[0];
    }

    @Override
    public double rightBound() {
        logger.trace("rightBound: {}", xValues[count - 1]);
        return xValues[count - 1];
    }

    // Прописываем реализацию абстрактных методов из AbstractTabulatedFunction
    @Override
    protected int floorIndexOfX(double x) {
        if (x < leftBound() || x > rightBound()) {
            logger.error("floorIndexOfX: x = {} выходит за границы [{}..{}]", x, leftBound(), rightBound());
            throw new IllegalArgumentException("x = " + x + " выходит за границы интервала");
        }

        for (int i = 0; i < count - 1; i++) {
            if (x >= xValues[i] && x < xValues[i + 1]) {
                logger.trace("floorIndexOfX: x={} -> {}", x, i);
                return i;
            }
        }
        logger.trace("floorIndexOfX: возврат последнего интервала {}", count - 2);
        return count - 2;
    }

    @Override
    protected double extrapolateLeft(double x) {
        logger.debug("extrapolateLeft: x={} result={}", x, interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]));
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        logger.debug("extrapolateRight: x={} result={}", x, interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]));
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {

        double leftX = xValues[floorIndex];
        double rightX = xValues[floorIndex + 1];
        if (x < leftX || x > rightX) {
            logger.error("interpolate: x = {} вне интервала интерполяции [{}, {}]", x, leftX, rightX);
            throw new ru.ssau.tk.avokado.lab2.exceptions.InterpolationException(
                    "x = " + x + " вне интервала интерполяции [" + leftX + ", " + rightX + "]"
            );
        }
        double leftY = yValues[floorIndex];
        double rightY = yValues[floorIndex + 1];
        logger.debug("interpolate: x={}, interval=[{},{}], y=[{},{}] -> {}", x, leftX, rightX, leftY, rightY, interpolate(x, leftX, rightX, leftY, rightY));
        return interpolate(x, leftX, rightX, leftY, rightY);
    }

    @Override
    public void insert(double x, double y) {
        // Поиск существующего x
        int index = indexOfX(x);
        if (index != -1) {
            logger.debug("insert: x={} найден, заменяем y на {}", x, y);
            setY(index, y);
            return;
        }

        // Создание новых массивов с запасом (+10)
        logger.debug("insert: вставка x={}, y={}", x, y);
        double[] newX = new double[count + 1 + 10];
        double[] newY = new double[count + 1 + 10];

        // Поиск позиции для вставки
        int i = 0;
        while (i < count && xValues[i] < x) {
            i++;
            logger.trace("insert: продвижение индекса i={}", i);
        }

        // Копирование элементов до позиции i
        System.arraycopy(xValues, 0, newX, 0, i);
        System.arraycopy(yValues, 0, newY, 0, i);

        // Вставка нового элемента
        newX[i] = x;
        newY[i] = y;

        // Копирование оставшихся элементов
        System.arraycopy(xValues, i, newX, i + 1, count - i);
        System.arraycopy(yValues, i, newY, i + 1, count - i);

        // Обновление ссылок
        this.xValues = newX;
        this.yValues = newY;
        this.count++;
        logger.info("insert: вставлено в позицию {}, новый count={}", i, count);
    }

    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
            logger.error("remove: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }

        double[] newX = new double[count - 1];
        double[] newY = new double[count - 1];

        // Копирование до index
        System.arraycopy(xValues, 0, newX, 0, index);
        System.arraycopy(yValues, 0, newY, 0, index);

        // Копирование после index
        System.arraycopy(xValues, index + 1, newX, index, count - index - 1);
        System.arraycopy(yValues, index + 1, newY, index, count - index - 1);

        this.xValues = newX;
        this.yValues = newY;
        this.count--;
        logger.info("remove: удалён индекс {}, новый count={}", index, count);
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    logger.error("next: NoSuchElementException");
                    throw new java.util.NoSuchElementException();
                }
                Point p = new Point(xValues[i], yValues[i]);
                logger.trace("iterator.next: i={}, point=({}, {})", i, p.x, p.y);
                i++;
                return p;
            }

            @Override
            public void remove() {
                logger.trace("iterator.remove: UnsupportedOperationException");
                throw new UnsupportedOperationException("remove");
            }
        };
    }

}
