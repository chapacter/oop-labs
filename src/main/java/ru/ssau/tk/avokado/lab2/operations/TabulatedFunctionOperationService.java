package ru.ssau.tk.avokado.lab2.operations;

import ru.ssau.tk.avokado.lab2.functions.Point;
import ru.ssau.tk.avokado.lab2.functions.TabulatedFunction;

public class TabulatedFunctionOperationService {

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("TabulatedFunction is null");
        }

        int count = tabulatedFunction.getCount();
        Point[] points = new Point[count];

        int i = 0;
        for (Point point : tabulatedFunction) {
            points[i++] = new Point(point.x, point.y);
        }

        return points;
    }
}
