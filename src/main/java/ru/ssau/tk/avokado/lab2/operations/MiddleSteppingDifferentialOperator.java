package ru.ssau.tk.avokado.lab2.operations;

import ru.ssau.tk.avokado.lab2.functions.MathFunction;

public class MiddleSteppingDifferentialOperator extends SteppingDifferentialOperator {

    public MiddleSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(final MathFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("function is null");
        }
        final double h = this.step;
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (function.apply(x + h) - function.apply(x - h)) / (2.0 * h);
            }
        };
    }
}
