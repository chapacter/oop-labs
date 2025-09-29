package ru.ssau.tk.avokado.lab2.functions;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction {
    private int count; // Количество узлов
    private Node head; // Ссылка на голову списка (null, если список пуст)

    // Приватный метод для добавления узла в конец списка
    private void addNode(double x, double y) {
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        if (head == null) { // если список пуст
            head = newNode;
            head.next = head;
            head.prev = head;
        } else { // добавление в конец
            Node last = head.prev;
            last.next = newNode;
            newNode.prev = last;
            newNode.next = head;
            head.prev = newNode;
        }
        count++;
    }

    // 🔹 Конструктор по массивам
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Длины массивов x и y должны совпадать!");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Должно быть >= 2 точки!");
        }
        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                throw new IllegalArgumentException("xValues должны быть строго возрастающими!");
            }
        }
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // 🔹 Конструктор по функции и интервалу
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть >= 2!");
        }
        if (xFrom > xTo) {
            double tmp = xFrom;
            xFrom = xTo;
            xTo = tmp;
        }
        double step = (xTo - xFrom) / (count - 1);
        for (int i = 0; i < count; i++) {
            double x = xFrom + i * step;
            addNode(x, source.apply(x));
        }
    }

    // 🔹 Вспомогательный метод для получения узла по индексу
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException();
        }
        Node current;
        if (index < count / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            current = head.prev;
            for (int i = count - 1; i > index; i--) {
                current = current.prev;
            }
        }
        return current;
    }

    // Реализация интерфейса TabulatedFunction
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x == x) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.y == y) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return head.x;
    }

    @Override
    public double rightBound() {
        return head.prev.x;
    }

    // Методы из AbstractTabulatedFunction
    @Override
    protected int floorIndexOfX(double x) {
        if (x < head.x) return 0;
        if (x > head.prev.x) return count;
        Node current = head;
        for (int i = 1; i < count; i++) {
            if (current.next.x > x) {
                return i - 1;
            }
            current = current.next;
        }
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        Node first = head;
        Node second = first.next;
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        Node last = head.prev;
        Node prev = last.prev;
        return interpolate(x, prev.x, last.x, prev.y, last.y);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        Node left = getNode(floorIndex);
        Node right = left.next;
        return interpolate(x, left.x, right.x, left.y, right.y);
    }
}
