package ru.ssau.tk.avokado.lab2.functions;

import java.util.Arrays;

// Прописываем класс ArrayTabulatedFunction для расширения класса AbstractTabulatedFunction
public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable {
    private double[] xValues;
    private double[] yValues;
    private int count;

    // Прописываем первый конструктор, состоящий из массивов x и y
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Длина таблицы < 2");
        }

        // Создаем копии массивов методом Arrays.copyOf()
        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);
        this.count = xValues.length;
    }

    // Прописываем второй конструктор для дискретизации функции
    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (source == null) {
            throw new IllegalArgumentException("функция = null");
        }
        if (getCount() < 2) {
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
    }

    // Прописываем реализацию методов для TabulatedFunction
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) { // Или лучше делать? "index >= getCount()"?
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(xValues[i] - x) < 1e-12) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(yValues[i] - y) < 1e-12) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return xValues[0];
    }

    @Override
    public double rightBound() {
        return xValues[count - 1];
    }

    // Прописываем реализацию абстрактных методов из AbstractTabulatedFunction
    @Override
    protected int floorIndexOfX(double x) {
        if (x < leftBound() || x > rightBound()) {
            throw new IllegalArgumentException("x = " + x + " выходит за границы интервала");
        }

        for (int i = 0; i < count - 1; i++) {
            if (x >= xValues[i] && x < xValues[i + 1]) {
                return i;
            }
        }
        return count - 2;
    }

    @Override
    protected double extrapolateLeft(double x) {
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (floorIndex < 0 || floorIndex >= count - 1) {
            throw new IllegalArgumentException("x выходит за границы интервала");
        }
        if (x < leftBound() || x > rightBound()) {
            throw new IllegalArgumentException("x выходит за границы интервала");
        }


        double leftX = xValues[floorIndex];
        double rightX = xValues[floorIndex + 1];
        double leftY = yValues[floorIndex];
        double rightY = yValues[floorIndex + 1];

        return interpolate(x, leftX, rightX, leftY, rightY);

    }


    @Override
    public void insert(double x, double y) {
        // Поиск существующего x
        int index = indexOfX(x);
        if (index != -1) {
            setY(index, y);
            return;
        }

        // Создание новых массивов с запасом (+10)
        double[] newX = new double[count + 1 + 10];
        double[] newY = new double[count + 1 + 10];

        // Поиск позиции для вставки
        int i = 0;
        while (i < count && xValues[i] < x) {
            i++;
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
    }

    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
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
    }
}