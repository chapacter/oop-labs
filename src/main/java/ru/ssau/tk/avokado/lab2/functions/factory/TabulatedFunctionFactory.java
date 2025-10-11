package ru.ssau.tk.avokado.lab2.functions.factory;

import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
}
