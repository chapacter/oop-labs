package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractTabulatedFunctionTest {

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
}