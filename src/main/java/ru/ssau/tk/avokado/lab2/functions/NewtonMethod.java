package ru.ssau.tk.avokado.lab2.functions;

public class NewtonMethod implements MathFunction {
    private final MathFunction function;
    private final MathFunction derivative;
    private final double toleranceX;
    private final double toleranceF;
    private final int maxIterations;
    private final double minDerivative;

    public NewtonMethod(MathFunction function, MathFunction derivative,
                        double toleranceX, double toleranceF, int maxIterations, double minDerivative) {
        this.function = function;
        this.derivative = derivative;
        this.toleranceX = toleranceX;
        this.toleranceF = toleranceF;
        this.maxIterations = maxIterations;
        this.minDerivative = minDerivative;
    }

    public NewtonMethod(MathFunction function, MathFunction derivative) {
        this(function, derivative, 1e-10, 1e-12, 1000, 1e-15);
    }

    @Override
    public double apply(double initialGuess) {
        double currentX = initialGuess;
        double bestX = initialGuess;
        double bestFunctionValue = Math.abs(function.apply(initialGuess));

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            double functionValue;
            double derivativeValue;

            try {
                functionValue = function.apply(currentX);
                derivativeValue = derivative.apply(currentX);
            } catch (Exception e) {
                // Если вычисление функции или производной вызывает исключение,
                // возвращаем лучшее найденное приближение
                return bestX;
            }

            // Проверка числовой стабильности
            if (!Double.isFinite(functionValue) || !Double.isFinite(derivativeValue)) {
                return bestX; // Возвращаем лучшую найденную точку вместо исключения
            }

            // Обновляем лучшую точку
            double absFunctionValue = Math.abs(functionValue);
            if (absFunctionValue < bestFunctionValue) {
                bestFunctionValue = absFunctionValue;
                bestX = currentX;
            }

            // Критерий остановки по значению функции
            if (absFunctionValue < toleranceF) {
                return currentX;
            }

            // Проверка производной
            if (Math.abs(derivativeValue) < minDerivative) {
                throw new ArithmeticException("Производная слишком мала в точке x=" + currentX);
            }

            // Шаг метода Ньютона
            double newtonStep = functionValue / derivativeValue;
            double nextX = currentX - newtonStep;

            // Демпфирование только если шаг ухудшает решение
            double nextFunctionValue;
            try {
                nextFunctionValue = function.apply(nextX);
            } catch (Exception e) {
                // Если не удалось вычислить функцию в новой точке,
                // возвращаем лучшее найденное приближение
                return bestX;
            }

            if (!Double.isFinite(nextFunctionValue)) {
                return bestX;
            }

            if (Math.abs(nextFunctionValue) > absFunctionValue) {
                // Пробуем уменьшить шаг
                double dampingFactor = 0.5;
                for (int dampingIteration = 0; dampingIteration < 10; dampingIteration++) {
                    nextX = currentX - dampingFactor * newtonStep;
                    try {
                        nextFunctionValue = function.apply(nextX);
                    } catch (Exception e) {
                        break; // Если не удалось вычислить, используем последнюю хорошую точку
                    }

                    if (!Double.isFinite(nextFunctionValue)) {
                        break;
                    }

                    if (Math.abs(nextFunctionValue) <= absFunctionValue) {
                        break; // Нашли хороший шаг
                    }
                    dampingFactor *= 0.5; // Продолжаем уменьшать
                }
            }

            // Проверка числовой стабильности новой точки
            if (!Double.isFinite(nextX)) {
                return bestX; // Возвращаем лучшую найденную точку
            }

            // Критерий остановки по изменению аргумента
            if (Math.abs(nextX - currentX) < toleranceX) {
                return nextX;
            }

            currentX = nextX;
        }

        // Если не сошлись, возвращаем лучшую найденную точку
        return bestX;
    }
}