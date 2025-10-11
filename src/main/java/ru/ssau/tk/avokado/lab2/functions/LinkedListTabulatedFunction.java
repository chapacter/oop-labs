package ru.ssau.tk.avokado.lab2.functions;

import java.util.Iterator;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements TabulatedFunction, Insertable, Removable {

    // Внутренний класс узла списка
    private static class Node {
        public Node next;
        public Node prev;
        public double x;
        public double y;

        public Node(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private Node head;      // Голова списка
    protected int count;    // Количество элементов

    // Добавление нового узла в конец списка
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

    // Конструктор с массивами xValues и yValues
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Длины массивов не совпадают");
        }
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // Конструктор с дискретизацией функции source
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Кол-во точек < 2");
        }
        if (xFrom > xTo) {
            double tmp = xFrom;
            xFrom = xTo;
            xTo = tmp;
        }
        double step = (xTo - xFrom) / (count - 1);
        for (int i = 0; i < count; i++) {
            double x = xFrom + step * i;
            double y = source.apply(x);
            addNode(x, y);
        }
    }

    // Получение узла по индексу
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Некорректный индекс: " + index);
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

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) { // Или лучше делать? "index >= getCount()"?
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }

        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }

        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }

        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x == x) return i;
            current = current.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.y == y) return i;
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

    @Override
    protected int floorIndexOfX(double x) {
        if (x < leftBound()) {
            throw new IllegalArgumentException("x: " + x + " < левой границы");
        }
        if (x >= rightBound()) return count - 1;

        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (current.x <= x && current.next.x > x) {
                return i;
            }
            current = current.next;
        }
        return count - 1; // на всякий случай
    }

    @Override
    protected double extrapolateLeft(double x) {
        Node first = head;
        Node second = head.next;
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        Node last = head.prev;
        Node beforeLast = last.prev;
        return interpolate(x, beforeLast.x, last.x, beforeLast.y, last.y);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        Node left = getNode(floorIndex);
        Node right = left.next;
        return interpolate(x, left.x, right.x, left.y, right.y);
    }

    @Override
    public void insert(double x, double y) {
        if (head == null) {
            addNode(x, y);
            return;
        }

        Node current = head;
        do {
            if (current.x == x) {
                current.y = y;
                return;
            }
            current = current.next;
        } while (current != head);

        // Вставка в нужную позицию
        current = head;
        Node newNode = new Node(x, y);
        do {
            if (current.x > x) {
                // Вставка перед current
                newNode.next = current;
                newNode.prev = current.prev;
                current.prev.next = newNode;
                current.prev = newNode;
                if (current == head) {
                    head = newNode;
                }
                count++;
                return;
            }
            current = current.next;
        } while (current != head);

        // Если дошли до конца - вставляем в конец
        addNode(x, y);
    }
        @Override
    public void remove(int index) {
        Node nodeToRemove = getNode(index);

        if (count == 1) {
            head = null;
        } else {
            nodeToRemove.prev.next = nodeToRemove.next;
            nodeToRemove.next.prev = nodeToRemove.prev;
            if (nodeToRemove == head) {
                head = head.next;
            }
        }
        count--;
    }


    @Override
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException("Итератор не поддерживается");
    }
}

