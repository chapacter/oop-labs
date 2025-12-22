package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class CompositeFunctionAndThenMathFunctionAfterFunctionTest {

    @Test
    void testAndThenWithDifferentFunctions() {
        MathFunction identity = new IdentityFunction();
        MathFunction sqr = new SqrFunction();
        MathFunction constant = new ConstantFunction(88.88);

        // Получаем операцию f(x) = x, g(x) = x² => h(x) = g(f(x)) = (x)²
        MathFunction identityThenSqr = identity.andThen(sqr);
        assertEquals(81, identityThenSqr.apply(-9), 1e-9);
        assertEquals(144, identityThenSqr.apply(12), 1e-9);

        // Получаем операцию f(x) = x², g(x) = 5 => h(x) = g(f(x)) = 5
        MathFunction sqrThenConstant = sqr.andThen(constant);
        assertEquals(88.88, sqrThenConstant.apply(45), 1e-9);
        assertEquals(88.88, sqrThenConstant.apply(-34), 1e-9);

        // Получаем операцию f(x) = 5, g(x) = x² => h(x) = g(f(x)) = 25
        MathFunction constantThenSqr = constant.andThen(sqr);
        assertEquals(7899.6544, constantThenSqr.apply(2), 1e-9);
        assertEquals(7899.6544, constantThenSqr.apply(100), 1e-9);
    }

    @Test
    void testAndThenWithNull() {
        MathFunction identity = new IdentityFunction();

        // Проверяем, что передача null вызовет исключение
        try {
            identity.andThen(null);
            fail("Ожидалось исключение IllegalArgumentException, но оно не было вызвано");
        } catch (IllegalArgumentException e) {
            assertEquals("Функция afterFunction не может равняться null!", e.getMessage());
        }
    }
}