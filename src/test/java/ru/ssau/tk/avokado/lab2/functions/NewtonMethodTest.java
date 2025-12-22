package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NewtonMethodTest {

    private MathFunction squareFunction;
    private MathFunction squareDerivative;
    private MathFunction cubicFunction;
    private MathFunction cubicDerivative;

    @BeforeEach
    void setUp() {
        squareFunction = x -> x * x - 4;
        squareDerivative = x -> 2 * x;

        cubicFunction = x -> x * x * x - 8;
        cubicDerivative = x -> 3 * x * x;
    }

    // Остальные тесты остаются без изменений...

    @Test
    void testNonFiniteFunctionValueReturnsBestApproximation() {
        // Функция, которая возвращает NaN после x > 5
        MathFunction specialFunction = x -> {
            if (x > 5) return Double.NaN;
            return x * x - 4;
        };
        MathFunction specialDerivative = x -> 2 * x;

        NewtonMethod newton = new NewtonMethod(specialFunction, specialDerivative);

        // Начинаем с точки, где функция определена, но следующая итерация приведет к NaN
        double result = newton.apply(4.0);

        // Должен вернуть лучшую найденную точку (4.0 или близкую к ней)
        assertTrue(Double.isFinite(result));
        assertTrue(result >= -10 && result <= 10); // Проверяем, что это разумное значение
    }

    @Test
    void testImmediateNaNReturnsInitialGuess() {
        // Функция, которая всегда возвращает NaN
        MathFunction nanFunction = x -> Double.NaN;
        MathFunction nanDerivative = x -> 1.0;

        NewtonMethod newton = new NewtonMethod(nanFunction, nanDerivative);

        double initialGuess = 5.0;
        double result = newton.apply(initialGuess);

        // Должен вернуть начальное приближение, так как это лучшее, что есть
        assertEquals(initialGuess, result, 1e-8);
    }

    @Test
    void testFunctionThrowingExceptionReturnsBestApproximation() {
        // Функция, которая бросает исключение при определенных значениях
        MathFunction throwingFunction = new MathFunction() {
            @Override
            public double apply(double x) {
                if (x > 3) {
                    throw new ArithmeticException("x too large");
                }
                return x * x - 4;
            }
        };

        MathFunction simpleDerivative = x -> 2 * x;

        NewtonMethod newton = new NewtonMethod(throwingFunction, simpleDerivative);

        // Начинаем с безопасного значения, но метод может выйти за пределы
        double result = newton.apply(2.0);

        // Должен вернуть лучшую найденную точку
        assertTrue(Double.isFinite(result));
        assertTrue(result <= 3); // Не должно превышать 3
    }

    @Test
    void testDampingWithNonFiniteValues() {
        // Функция, которая становится NaN при определенных значениях
        MathFunction problematicFunction = x -> {
            if (x < 1 || x > 3) return Double.NaN;
            return x * x - 4;
        };

        MathFunction problematicDerivative = x -> 2 * x;

        NewtonMethod newton = new NewtonMethod(problematicFunction, problematicDerivative);

        // Начинаем с точки, где функция определена
        double result = newton.apply(2.0);

        // Должен вернуть точку в допустимом диапазоне
        assertTrue(Double.isFinite(result));
        assertTrue(result >= 1 && result <= 3);
    }

    // Остальные тесты из предыдущего варианта...
    @Test
    void testBasicConvergence() {
        NewtonMethod newton = new NewtonMethod(squareFunction, squareDerivative);
        double root = newton.apply(3.0);

        assertEquals(2.0, root, 1e-8);
        assertEquals(0.0, squareFunction.apply(root), 1e-8);
    }

    @Test
    void testConvergenceWithToleranceF() {
        NewtonMethod newton = new NewtonMethod(squareFunction, squareDerivative, 1e-10, 1e-8, 100, 1e-15);
        double root = newton.apply(1.5);

        assertEquals(2.0, root, 1e-6);
    }

    @Test
    void testConvergenceWithToleranceX() {
        NewtonMethod newton = new NewtonMethod(cubicFunction, cubicDerivative);
        double root = newton.apply(3.0);

        assertEquals(2.0, root, 1e-8);
        assertEquals(0.0, cubicFunction.apply(root), 1e-8);
    }

    @Test
    void testZeroDerivativeThrowsException() {
        MathFunction constantFunction = x -> 5.0;
        MathFunction zeroDerivative = x -> 0.0;

        NewtonMethod newton = new NewtonMethod(constantFunction, zeroDerivative);

        assertThrows(ArithmeticException.class, () -> newton.apply(1.0));
    }

    @Test
    void testMaxIterationsReturnsBestApproximation() {
        MathFunction slowFunction = x -> Math.exp(-x) - 0.001;
        MathFunction slowDerivative = x -> -Math.exp(-x);

        NewtonMethod newton = new NewtonMethod(slowFunction, slowDerivative, 1e-10, 1e-10, 5, 1e-15);

        double result = newton.apply(1.0);
        assertTrue(Double.isFinite(result));
    }
}
