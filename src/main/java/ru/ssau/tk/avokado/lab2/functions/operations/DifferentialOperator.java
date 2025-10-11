package ru.ssau.tk.avokado.lab2.functions.operations;

import ru.ssau.tk.avokado.lab2.functions.MathFunction;

public interface DifferentialOperator<T extends MathFunction> {
    T derive(T function);
}
