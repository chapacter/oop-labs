package ru.ssau.tk.avokado.functions;

public class CompositeFunction implements MathFunction{
    private final MathFunction firstFun;
    private final MathFunction secondFun;

    public CompositeFunction(MathFunction firstFun, MathFunction secondFun) {
        this.firstFun = firstFun;
        this.secondFun = secondFun;
    }

    @Override
    public double apply(double x) {
        return secondFun.apply(firstFun.apply(x));
    }
}
