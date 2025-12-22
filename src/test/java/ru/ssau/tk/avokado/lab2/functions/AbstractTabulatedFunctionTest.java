package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractTabulatedFunctionTest {
    @Test
    public void testToStringLinkedListFunction() {
        double[] x = {0.0, 0.5, 1.0};
        double[] y = {0.0, 0.25, 1.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        String expected = "LinkedListTabulatedFunction size = 3\n" +
                "[0.0; 0.0]\n" +
                "[0.5; 0.25]\n" +
                "[1.0; 1.0]";
        assertEquals(expected, f.toString());
    }

    @Test
    public void testToString() {
        double[] xValues = {0.0, 0.5, 1.0};
        double[] yValues = {0.0, 0.25, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        String expected = "ArrayTabulatedFunction size = 3\n[0.0; 0.0]\n[0.5; 0.25]\n[1.0; 1.0]";
        String actual = function.toString();

        assertEquals(expected, actual, "Строковое представление функции не соответствует ожидаемому");
    }

    @Test
    public void testToStringWithLinkedListFunction() {
        double[] xValues = {0.0, 0.5, 1.0};
        double[] yValues = {0.0, 0.25, 1.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        String expected = "LinkedListTabulatedFunction size = 3\n[0.0; 0.0]\n[0.5; 0.25]\n[1.0; 1.0]";
        String actual = function.toString();

        assertEquals(expected, actual, "Строковое представление функции не соответствует ожидаемому");
    }

    @Test
    public void testToStringWithTwoPoints() {
        double[] xValues = {0.0, 1.0};
        double[] yValues = {0.0, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        String expected = "ArrayTabulatedFunction size = 2\n[0.0; 0.0]\n[1.0; 1.0]";
        String actual = function.toString();

        assertEquals(expected, actual, "Строковое представление функции с двумя точками не соответствует ожидаемому");
    }
}