package ru.ssau.tk.avokado.functions;

import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexFunctionsTest {

    @Test
    public void testCompositeWithArrayTabulatedAndLinkedListTabulated() {
        // Создаем табулированную функцию на массиве
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);

        // Создаем табулированную функцию на списке
        MathFunction source = new SqrFunction();
        TabulatedFunction linkedListFunc = new LinkedListTabulatedFunction(source, 0.0, 2.0, 3);

        // Создаем сложную функцию: linkedListFunc(arrayFunc(x))
        CompositeFunction composite1 = new CompositeFunction(arrayFunc, linkedListFunc);
        assertEquals(0.0, composite1.apply(0.0), 1e-9);
        assertEquals(1.0, composite1.apply(1.0), 1e-9);
        assertEquals(16.0, composite1.apply(2.0), 1e-9); // (2^2)^2 = 16

        // Создаем сложную функцию: arrayFunc(linkedListFunc(x))
        CompositeFunction composite2 = new CompositeFunction(linkedListFunc, arrayFunc);
        assertEquals(0.0, composite2.apply(0.0), 1e-9);
        assertEquals(1.0, composite2.apply(1.0), 1e-9);
        assertEquals(4.0, composite2.apply(2.0), 1e-9); // (2^2) = 4
    }

    @Test
    public void testCompositeWithTabulatedAndMathFunction() {
        // Табулированная функция на массиве
        double[] xValues = {-1.0, 0.0, 1.0};
        double[] yValues = {-2.0, 0.0, 2.0};
        TabulatedFunction tabulatedFunc = new ArrayTabulatedFunction(xValues, yValues);

        // Аналитическая функция
        MathFunction sqrFunction = new SqrFunction();

        // Создаем сложную функцию: sqrFunction(tabulatedFunc(x))
        CompositeFunction composite1 = new CompositeFunction(tabulatedFunc, sqrFunction);
        assertEquals(4.0, composite1.apply(-1.0), 1e-9); // (-2)^2 = 4
        assertEquals(0.0, composite1.apply(0.0), 1e-9);
        assertEquals(4.0, composite1.apply(1.0), 1e-9); // (2)^2 = 4

        // Создаем сложную функцию: tabulatedFunc(sqrFunction(x))
        CompositeFunction composite2 = new CompositeFunction(sqrFunction, tabulatedFunc);
        assertEquals(0.0, composite2.apply(0.0), 1e-9);
        assertEquals(2.0, composite2.apply(1.0), 1e-9); // tabulatedFunc(1) = 2
    }

    @Test
    public void testChainWithAndThen() {
        MathFunction identity = new IdentityFunction();
        MathFunction sqr = new SqrFunction();
        MathFunction constant = new ConstantFunction(2.0);

        // Строим цепочку: constant(sqr(identity(x)))
        MathFunction chain = identity.andThen(sqr).andThen(constant);
        assertEquals(2.0, chain.apply(0.0), 1e-9); // 2 * (0^2) = 0? Нет, constant всегда возвращает 2
        assertEquals(2.0, chain.apply(5.0), 1e-9); // constant игнорирует результат sqr

        // Цепочка с табулированной функцией
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 3.0};
        TabulatedFunction tabulatedFunc = new LinkedListTabulatedFunction(xValues, yValues);

        MathFunction chain2 = sqr.andThen(tabulatedFunc);
        assertEquals(0.0, chain2.apply(0.0), 1e-9);
        assertEquals(3.0, chain2.apply(1.0), 1e-9); // tabulatedFunc(1) = 3
        assertEquals(3.0, chain2.apply(2.0), 1e-9); // tabulatedFunc(4) - экстраполяция
    }

    @Test
    public void testCompositeOfComposites() {
        MathFunction f1 = new IdentityFunction();
        MathFunction f2 = new SqrFunction();
        MathFunction f3 = new ConstantFunction(5.0);

        // h(x) = f3(f2(f1(x)))
        CompositeFunction compositeInner = new CompositeFunction(f1, f2);
        CompositeFunction compositeOuter = new CompositeFunction(compositeInner, f3);

        assertEquals(5.0, compositeOuter.apply(0.0), 1e-9);
        assertEquals(5.0, compositeOuter.apply(10.0), 1e-9); // f3 всегда возвращает 5

        // Альтернативно через andThen
        MathFunction chain = f1.andThen(f2).andThen(f3);
        assertEquals(5.0, chain.apply(0.0), 1e-9);
    }
}