package ru.ssau.tk.avokado.functions;

public interface MathFunction {
    double apply(double x);

    // Добавляем метод для композиции функции с реализацией по умолчанию
    default CompositeFunction andThen(MathFunction afterFunction) {
        if (afterFunction == null) {
            throw new IllegalArgumentException("Функция afterFunction не может равняться null!");
        }
        // Возвращаем получившуюся сложную функцию
        return new CompositeFunction(this, afterFunction);
    }
}

