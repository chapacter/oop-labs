package ru.ssau.tk.avokado.lab2.functions;

public class IdentityFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x;
    }
}