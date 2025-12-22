package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitFunctionTest {

    @Test
    void testUnitFunction() {
        UnitFunction unit = new UnitFunction();

        assertEquals(1.0, unit.apply(16), 1e-9);
        assertEquals(1.0, unit.apply(0), 1e-9);
        assertEquals(1.0, unit.apply(-5.91), 1e-9);

        // Проверяем тестом, что getConstant тоже возвращает 1
        assertEquals(1.0, unit.getConstant(), 1e-9);
    }
}