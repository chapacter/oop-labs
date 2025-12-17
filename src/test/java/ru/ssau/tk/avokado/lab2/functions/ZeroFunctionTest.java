package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZeroFunctionTest {

    @Test
    void testZeroFunction() {
        ZeroFunction zero = new ZeroFunction();

        assertEquals(0.0, zero.apply(16), 1e-9);
        assertEquals(0.0, zero.apply(0), 1e-9);
        assertEquals(0.0, zero.apply(-5.91), 1e-9);

        // Проверяем тестом, что getConstant тоже возвращает 0
        assertEquals(0.0, zero.getConstant(), 1e-9);
    }
}
