package ru.ssau.tk.avokado.lab2.functions;

public class DeBoorFunction implements MathFunction { // Хотя по факту это Bspline
    private final double[] nodes; // узлы (nodes)
    private final double[] controlPoints; // контрольные точки
    private final int degree; // степень сплайна

    public DeBoorFunction(double[] nodes, double[] controlPoints, int degree) {
        if (nodes.length < controlPoints.length + degree + 1) {
            throw new IllegalArgumentException("Некорректные размеры массивов узлов и контрольных точек");
        }
        this.nodes = nodes;
        this.controlPoints = controlPoints;
        this.degree = degree;
    }

    @Override
    public double apply(double x) {
        int n = controlPoints.length - 1;

        // нахождение интервала [nodes[i], nodes[i+1]) где лежит x
        int k = findNodeSpan(x, n, degree, nodes);

        // скопировать соответствующие контрольные точки
        double[] d = new double[degree + 1];
        System.arraycopy(controlPoints, k - degree, d, 0, degree + 1);

        // сам алгоритм де Бура
        for (int r = 1; r <= degree; r++) {
            for (int j = degree; j >= r; j--) {
                int i = k - degree + j;
                double alpha = (x - nodes[i]) / (nodes[i + degree - r + 1] - nodes[i]);
                d[j] = (1.0 - alpha) * d[j - 1] + alpha * d[j];
            }
        }
        return d[degree];
    }

    private int findNodeSpan(double x, int n, int p, double[] U) {
        if (x >= U[n + 1]) {
            return n;
        }
        if (x <= U[p]) {
            return p;
        }
        int low = p;
        int high = n + 1;
        int mid = (low + high) / 2;
        while (x < U[mid] || x >= U[mid + 1]) {
            if (x < U[mid]) {
                high = mid;
            } else {
                low = mid;
            }
            mid = (low + high) / 2;
        }
        return mid;
    }
}
