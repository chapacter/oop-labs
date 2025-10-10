package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.MathFunction;
import ru.ssau.tk.avokado.lab2.functions.SqrFunction;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionTest {

    @Test
    void testConstructorWithArrays() {
        double[] x = {0, 1, 2};
        double[] y = {0, 2, 4};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(3, f.getCount());
        assertEquals(0, f.getX(0));
        assertEquals(2, f.getY(1));
    }

    @Test
    void testConstructorWithFunction() {
        MathFunction sqr = new SqrFunction();
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(sqr, 0, 2, 3);

        assertEquals(3, f.getCount());
        assertEquals(0.0, f.getX(0));
        assertEquals(1.0, f.getY(1)); // 1^2 = 1
        assertEquals(4.0, f.getY(2)); // 2^2 = 4
    }

    @Test
    void testGetSetY() {
        double[] x = {0, 1};
        double[] y = {10, 20};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(10, f.getY(0));
        f.setY(0, 15);
        assertEquals(15, f.getY(0));
    }

    @Test
    void testIndexOfXAndY() {
        double[] x = {0, 1, 2};
        double[] y = {5, 10, 15};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(1, f.indexOfX(1));
        assertEquals(-1, f.indexOfX(10));
        assertEquals(2, f.indexOfY(15));
        assertEquals(-1, f.indexOfY(100));
    }

    @Test
    void testBounds() {
        double[] x = {0, 1, 2};
        double[] y = {5, 10, 15};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(0, f.leftBound());
        assertEquals(2, f.rightBound());
    }

    @Test
    void testApplyInsideInterval() {
        double[] x = {0, 1, 2};
        double[] y = {0, 2, 4};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        // Интерполяция в середине
        assertEquals(1.0, f.apply(0.5), 1e-9);
        assertEquals(3.0, f.apply(1.5), 1e-9);
    }

    @Test
    void testApplyOutsideInterval() {
        double[] x = {0, 1, 2};
        double[] y = {0, 2, 4};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        // Экстраполяция на краях
        assertEquals(-2.0, f.apply(-1));
        assertEquals(6.0, f.apply(3));
    }

    @Test
    void testSinglePointFunction() {
        double[] x = {1};
        double[] y = {10};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(10, f.apply(1));
    }
    // Добавить в LinkedListTabulatedFunctionTest.java

    @Test
    void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(1.0, 10.0);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-12);
        assertEquals(10.0, function.getY(0), 1e-12);
        assertEquals(2.0, function.getX(1), 1e-12);
    }

    @Test
    void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(4.0, 40.0);

        assertEquals(4, function.getCount());
        assertEquals(4.0, function.rightBound(), 1e-12);
        assertEquals(40.0, function.getY(3), 1e-12);
    }

    @Test
    void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 4.0};
        double[] yValues = {10.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 25.0);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(2.0, function.getX(1), 1e-12);
        assertEquals(3.0, function.getX(2), 1e-12);
        assertEquals(25.0, function.getY(1), 1e-12);
    }

    @Test
    void testInsertReplaceExisting() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 25.0);

        assertEquals(3, function.getCount());
        assertEquals(25.0, function.getY(1), 1e-12);
        assertEquals(2.0, function.getX(1), 1e-12);
    }

    @Test
    void testInsertIntoEmptyList() {
        // Если есть конструктор для пустого списка
        double[] xValues = {};
        double[] yValues = {};
        // Этот тест может не понадобиться, так как конструктор требует непустые массивы
    }

    @Test
    void testInsertMaintainsCircularStructure() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(1.5, 15.0);

        // Проверяем, что структура осталась циклической
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-12);
        assertEquals(2.0, function.rightBound(), 1e-12);

        // Проверяем связи
        assertEquals(function.getX(0), function.leftBound(), 1e-12);
        assertEquals(function.getX(2), function.rightBound(), 1e-12);
    }
    @Test
    void testRemoveFromBeginning() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(3, function.getCount());
        assertEquals(2.0, function.leftBound(), 1e-12);
        assertEquals(20.0, function.getY(0), 1e-12);
        assertEquals(4.0, function.rightBound(), 1e-12);
    }

    @Test
    void testRemoveFromEnd() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(3);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-12);
        assertEquals(3.0, function.rightBound(), 1e-12);
        assertEquals(30.0, function.getY(2), 1e-12);
    }

    @Test
    void testRemoveFromMiddle() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(1);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(3.0, function.getX(1), 1e-12);
        assertEquals(4.0, function.getX(2), 1e-12);
        assertEquals(30.0, function.getY(1), 1e-12);
    }

    @Test
    void testRemoveInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> function.remove(3));
    }

    @Test
    void testRemoveSingleElement() {
        double[] xValues = {1.0};
        double[] yValues = {10.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(0, function.getCount());
        // Проверяем, что список стал пустым
        assertThrows(IllegalArgumentException.class, () -> function.getX(0));
    }

    @Test
    void testRemoveHeadMaintainsStructure() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(2, function.getCount());
        // Проверяем, что новая голова корректна
        assertEquals(2.0, function.leftBound(), 1e-12);
        assertEquals(3.0, function.rightBound(), 1e-12);

        // Проверяем циклическую структуру
        assertEquals(function.getX(0), function.leftBound(), 1e-12);
        assertEquals(function.getX(1), function.rightBound(), 1e-12);
    }

    @Test
    void testRemoveAndMaintainCircularLinks() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(1);

        assertEquals(2, function.getCount());
        // Проверяем, что связи остались корректными
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(3.0, function.getX(1), 1e-12);

        // Проверяем, что prev и next ссылки корректны
        assertEquals(function.getX(1), function.rightBound(), 1e-12);
        assertEquals(function.getX(0), function.leftBound(), 1e-12);
    }


    @Test
    public void testApply() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Точное совпадение
        assertEquals(0.0, function.apply(0.0), 1e-12);
        assertEquals(1.0, function.apply(1.0), 1e-12);

        // Интерполяция
        assertEquals(0.5, function.apply(0.5), 1e-12);
        assertEquals(2.5, function.apply(1.5), 1e-12);

        // Экстраполяция
        assertEquals(-1.0, function.apply(-1.0), 1e-12); // left
        assertEquals(7.0, function.apply(3.0), 1e-12);   // right
    }
}



