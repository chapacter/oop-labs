package ru.ssau.tk.avokado.lab2.functions.factory;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionFactoryTest {

    @Test
    void create() {
        double[] x = {1, 2, 3};
        double[] y = {2, 4, 6};
        TabulatedFunction function = new LinkedListTabulatedFunctionFactory().create(x, y);
        assertTrue(function instanceof LinkedListTabulatedFunction);
    }
}