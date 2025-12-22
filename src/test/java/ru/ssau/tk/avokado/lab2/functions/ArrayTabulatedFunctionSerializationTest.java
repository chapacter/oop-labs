package ru.ssau.tk.avokado.lab2.functions;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ArrayTabulatedFunctionSerializationTest {

    @Test
    public void testSerializeDeserialize() throws IOException, ClassNotFoundException {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(x, y);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        ois.close();

        assertInstanceOf(ArrayTabulatedFunction.class, obj);
        ArrayTabulatedFunction restored = (ArrayTabulatedFunction) obj;

        assertEquals(original.getCount(), restored.getCount());
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), restored.getX(i), 1e-12);
            assertEquals(original.getY(i), restored.getY(i), 1e-12);
        }
    }
}
