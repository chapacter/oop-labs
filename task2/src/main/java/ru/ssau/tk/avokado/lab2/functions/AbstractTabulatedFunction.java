package ru.ssau.tk.avokado.lab2.functions;

public abstract class AbstractTabulatedFunction implements TabulatedFunction {

    // Прописываем абстрактные защищённые методы
    protected abstract int floorIndexOfX(double x);
    protected abstract double extrapolateLeft(double x);
    protected abstract double extrapolateRight(double x);
    protected abstract double interpolate(double x, int floorIndex);

    // Прописываем защищённый метод интерполяции с реализацией по формуле из методички
    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    // Реализовываем метод apply, который был прописан в MathFunction
    @Override
    public double apply(double x) {
        if (x < leftBound()) {
            return extrapolateLeft(x); // Используем левую экстраполяцию, если x меньше левой границы
        } else if (x > rightBound()) {
            return extrapolateRight(x); // Используем правую экстраполяцию, если x больше правой границы
        } else {
            int index = indexOfX(x);
            if (index != -1) {
                return getY(index); // Возвращаем значение y для x, если x изначально находился в таблице
                // В противном случае вызываем метод интерполяции с указанием индекса интервала, предварительно отыскав его с помощью метода floorIndexOfX(double x).
            } else {
                int floorIndex = floorIndexOfX(x);
                return interpolate(x, floorIndex);
            }
        }
    }
}