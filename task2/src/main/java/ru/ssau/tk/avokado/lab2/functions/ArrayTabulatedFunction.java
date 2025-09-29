package ru.ssau.tk.avokado.lab2.functions;

import java.util.Arrays;

// Прописываем класс ArrayTabulatedFunction для расширения класса AbstractTabulatedFunction
public class ArrayTabulatedFunction extends AbstractTabulatedFunction {
    private final double[] xValues;
    private final double[] yValues;
    private final int count;

    // Прописываем первый конструктор, состоящий из массивов x и y
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {

        // Создаем копии массивов методом Arrays.copyOf()
        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);
        this.count = xValues.length;
    }

    // Прописываем второй конструктор для дискретизации функции
    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {

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
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
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
        if (x < xValues[0]) {
            return 0;
        }
        if (x > xValues[count - 1]) {
            return count;
        }

        for (int i = 0; i < count - 1; i++) {
            if (x >= xValues[i] && x < xValues[i + 1]) {
                return i;
            }
        }
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        if (count == 1) {
            return yValues[0];
        }
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        if (count == 1) {
            return yValues[0];
        }
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (count == 1) {
            return yValues[0];
        }

        if (floorIndex < 0) {
            return extrapolateLeft(x);
        }
        if (floorIndex >= count - 1) {
            return extrapolateRight(x);
        }

        double leftX = xValues[floorIndex];
        double rightX = xValues[floorIndex + 1];
        double leftY = yValues[floorIndex];
        double rightY = yValues[floorIndex + 1];

        return interpolate(x, leftX, rightX, leftY, rightY);
    }
}