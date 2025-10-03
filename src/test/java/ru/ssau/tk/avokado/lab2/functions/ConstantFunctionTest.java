package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.avokado.lab2.functions.ConstantFunction;

import static org.junit.jupiter.api.Assertions.*;

class ConstantFunctionTest {

    @Test
    void testConstantFunction() {
        // Прописываем различные константы для дальнейшего тестирования
        ConstantFunction func1 = new ConstantFunction(75.0);
        ConstantFunction func2 = new ConstantFunction(-451.1941);
        ConstantFunction func3 = new ConstantFunction(0.0);
        ConstantFunction func4 = new ConstantFunction(893.128);

        assertEquals(75.0, func1.apply(107.1), 1e-9);
        assertEquals(75.0, func1.apply(0.0), 1e-9);
        assertEquals(75.0, func1.apply(-50.43), 1e-9);

        assertEquals(-451.1941, func2.apply(10), 1e-9);
        assertEquals(0.0, func3.apply(999), 1e-9);
        assertEquals(893.128, func4.apply(9099), 1e-9);
    }

    // Прописываем тест для публичного метода getConstant()
    @Test
    void testGetConstantMethod() {
        ConstantFunction func = new ConstantFunction(894.3);
        assertEquals(894.3, func.getConstant(), 1e-9);
    }
}