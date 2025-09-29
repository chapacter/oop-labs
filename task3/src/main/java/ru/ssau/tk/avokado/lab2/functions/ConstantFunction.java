package ru.ssau.tk.avokado.functions;


public class ConstantFunction implements MathFunction {
    private final double constant; // Создаём приватное завершённое поле для константы

    // Создаём публичный конструктор с константным числом
    public ConstantFunction(double constant) {
        this.constant = constant;
    }

    @Override
    public double apply(double x) {
        return constant; // Возвращаем всегда одну и ту же константу независимо от того, какой x мы получили
    }

    // Добавляем публичный метод get для этой константы
    public double getConstant() {
        return constant;

    }
}