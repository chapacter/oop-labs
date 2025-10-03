package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

class ArrayTabulatedFunctionTest {

    @Test
    void testGetCount() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(2, function.getCount());
    }

    @Test
    void testGetX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(2.0, function.getX(1), 1e-12);
        assertEquals(3.0, function.getX(2), 1e-12);
    }

    @Test
    void testGetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(10.0, function.getY(0), 1e-12);
        assertEquals(20.0, function.getY(1), 1e-12);
        assertEquals(30.0, function.getY(2), 1e-12);
    }

    @Test
    void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), 1e-12);

        assertEquals(10.0, function.getY(0), 1e-12);
        assertEquals(30.0, function.getY(2), 1e-12);
    }

    @Test
    void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1, function.indexOfX(2.0));
        assertEquals(3, function.indexOfX(4.0));

        assertEquals(-1, function.indexOfX(2.5));
        assertEquals(-1, function.indexOfX(0.0));
        assertEquals(-1, function.indexOfX(5.0));

        assertEquals(2, function.indexOfX(3.0 + 1e-13));
    }

    @Test
    void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfY(10.0));
        assertEquals(2, function.indexOfY(30.0));

        assertEquals(-1, function.indexOfY(15.0));
        assertEquals(-1, function.indexOfY(0.0));
        assertEquals(-1, function.indexOfY(50.0));

        assertEquals(1, function.indexOfY(20.0 + 1e-13)); // Почти 20.0
    }


    @Test
    void testLeftBound() {
        double[] xValues = {1.5, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.5, function.leftBound(), 1e-12);
    }

    @Test
    void testRightBound() {
        double[] xValues = {1.0, 2.0, 3.5};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(3.5, function.rightBound(), 1e-12);
    }

    @Test
    void testFloorIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Проверяем левее всех точек
        assertEquals(0, function.floorIndexOfX(0.5));

        // Проверяем правее всех точек
        assertEquals(4, function.floorIndexOfX(5.0));

        // Проверяем между точками
        assertEquals(1, function.floorIndexOfX(2.5)); // Между 2.0 и 3.0
        assertEquals(2, function.floorIndexOfX(3.5)); // Между 3.0 и 4.0

        // Проверяем точное совпадение с точкой (должен вернуть индекс левой границы интервала)
        assertEquals(2, function.floorIndexOfX(3.0)); // x=3.0 → интервал [2,3)
        assertEquals(1, function.floorIndexOfX(2.0)); // x=2.0 → интервал [1,2)
    }

    @Test
    void testExtrapolateLeft() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.extrapolateLeft(0.5);
        assertEquals(5.0, result, 1e-12); // 10 * 0.5 = 5.0
    }

    @Test
    void testExtrapolateRight() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Экстраполяция справа: используем последние две точки
        // Уравнение прямой через (2,20) и (3,30): y = 10x
        double result = function.extrapolateRight(4.0);
        assertEquals(40.0, result, 1e-12); // 10 * 4.0 = 40.0
    }

    @Test
    void testInterpolateWithIndex() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Интерполяция в середине интервала
        double result = function.interpolate(2.5, 1); // Между 2.0 и 3.0
        assertEquals(25.0, result, 1e-12); // (20 + 30)/2 = 25.0

        // Проверяем граничные случаи
        assertEquals(0.0, function.interpolate(0.0, -1), 1e-12); // Левая экстраполяция
        assertEquals(50.0, function.interpolate(5.0, 3), 1e-12);  // Правая экстраполяция
    }

    @Test
    void testInterpolateWithSinglePoint() {
        // Тест с одной точкой
        double[] xValues = {5.0};
        double[] yValues = {50.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(50.0, function.interpolate(10.0, 0), 1e-12);
        assertEquals(50.0, function.interpolate(-5.0, -1), 1e-12);
    }

    @Test
    void testApply() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(10.0, function.apply(1.0), 1e-12);
        assertEquals(20.0, function.apply(2.0), 1e-12);
        assertEquals(30.0, function.apply(3.0), 1e-12);

        assertEquals(15.0, function.apply(1.5), 1e-12);
        assertEquals(25.0, function.apply(2.5), 1e-12);

        // Экстраполяция слева
        assertEquals(5.0, function.apply(0.5), 1e-12);

        // Экстраполяция справа
        assertEquals(40.0, function.apply(4.0), 1e-12);
    }

    @Test
    void testApplyWithSinglePoint() {
        // Проверяем особый случай, когда одна точка
        double[] xValues = {5.0};
        double[] yValues = {50.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(50.0, function.apply(10.0), 1e-12);
        assertEquals(50.0, function.apply(-5.0), 1e-12);
        assertEquals(50.0, function.apply(5.0), 1e-12);
    }
    @Test
    void testExtrapolateWithSinglePoint() {
        double[] xValues = {5.0};
        double[] yValues = {50.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        assertEquals(50.0, function.extrapolateLeft(0.0), 1e-12);
        assertEquals(50.0, function.extrapolateRight(10.0), 1e-12);
    }


    // Мы устали и больше не хотим кранчить((
    @Test
    void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(1.0, 10.0);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(10.0, function.getY(0), 1e-12);
        assertEquals(2.0, function.getX(1), 1e-12);
    }

    @Test
    void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(4.0, 40.0);

        assertEquals(4, function.getCount());
        assertEquals(3.0, function.getX(2), 1e-12);
        assertEquals(4.0, function.getX(3), 1e-12);
        assertEquals(40.0, function.getY(3), 1e-12);
    }

    @Test
    void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 4.0};
        double[] yValues = {10.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

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
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 25.0);

        assertEquals(3, function.getCount()); // Количество не должно измениться
        assertEquals(25.0, function.getY(1), 1e-12); // Значение должно обновиться
        assertEquals(2.0, function.getX(1), 1e-12); // X остался прежним
    }

    @Test
    void testInsertIntoEmptyArray() {
        double[] xValues = {};
        double[] yValues = {};
    }

    @Test
    void testInsertWithCapacity() {
        // Тест для случая, когда есть запас в массивах
        double[] xValues = new double[10];
        double[] yValues = new double[10];
        xValues[0] = 1.0; yValues[0] = 10.0;
        xValues[1] = 2.0; yValues[1] = 20.0;
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(
                Arrays.copyOf(xValues, 2), Arrays.copyOf(yValues, 2)
        );

        function.insert(1.5, 15.0);

        assertEquals(3, function.getCount());
        assertEquals(1.5, function.getX(1), 1e-12);
    }


    @Test
    void testRemoveFromBeginning() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(3, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-12);
        assertEquals(20.0, function.getY(0), 1e-12);
        assertEquals(4.0, function.getX(2), 1e-12);
    }

    @Test
    void testRemoveFromEnd() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(3);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(3.0, function.getX(2), 1e-12);
        assertEquals(30.0, function.getY(2), 1e-12);
    }

    @Test
    void testRemoveFromMiddle() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

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
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(IndexOutOfBoundsException.class, () -> function.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> function.remove(3));
    }

    @Test
    void testRemoveSingleElement() {
        double[] xValues = {1.0};
        double[] yValues = {10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(0, function.getCount());
        // После удаления единственного элемента функция становится пустой
    }

    @Test
    void testRemoveAndThenInsert() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(1);
        function.insert(2.5, 25.0);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-12);
        assertEquals(2.5, function.getX(1), 1e-12);
        assertEquals(3.0, function.getX(2), 1e-12);
        assertEquals(25.0, function.getY(1), 1e-12);
    }
}
