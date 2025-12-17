package ru.ssau.tk.avokado.lab2.operations;

import ru.ssau.tk.avokado.lab2.functions.MathFunction;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {

    protected double step;

    protected SteppingDifferentialOperator(double step) {
        validateStep(step);
        this.step = step;
    }

    private static void validateStep(double step) {
        if (!(step > 0) || Double.isInfinite(step) || Double.isNaN(step)) {
            throw new IllegalArgumentException("step must be positive finite and not NaN");
        }
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        validateStep(step);
        this.step = step;
    }
}
