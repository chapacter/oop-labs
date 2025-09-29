package ru.ssau.tk.avokado.functions;

public class LinkedListTabulatedFunction implements TabulatedFunction {

    private Node head;
    protected int count;

    // Конструктор: два массива x и y
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Массивы разной длины");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Массивы должны содержать хотя бы 2 элемента");
        }
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // Конструктор: MathFunction, интервал, количество точек
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (xFrom > xTo) {
            double tmp = xFrom;
            xFrom = xTo;
            xTo = tmp;
        }
        if (count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть >= 2");
        }
        double step = (xTo - xFrom) / (count - 1);
        double x = xFrom;
        for (int i = 0; i < count; i++) {
            addNode(x, source.apply(x));
            x += step;
        }
    }

    // Добавление узла в конец списка
    private void addNode(double x, double y) {
        Node newNode = new Node(x, y);
        if (head == null) {
            head = newNode;
            head.next = head;
            head.prev = head;
        } else {
            Node last = head.prev;
            last.next = newNode;
            newNode.prev = last;
            newNode.next = head;
            head.prev = newNode;
        }
        count++;
    }

    // Получение узла по индексу
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Некорректный индекс");
        }
        Node current;
        if (index < count / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
