package ru.ssau.tk.avokado.lab2.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;


public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements TabulatedFunction, Insertable, Removable, Serializable {
    @Serial
    private static final long serialVersionUID = -6692951459863380154L;
    private static final Logger logger = LoggerFactory.getLogger(LinkedListTabulatedFunction.class);
    protected int count;    // Количество элементов
    private Node head;      // Голова списка
    // Конструктор с массивами xValues и yValues
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);
        if (xValues.length == 0) {
            logger.error("LinkedListTabulatedFunction: длина списка == 0");
            throw new IllegalArgumentException("Длина таблицы < 1");
        }
        AbstractTabulatedFunction.checkSorted(xValues);
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
        logger.info("LinkedListTabulatedFunction создана с {} точками", xValues.length);
    }

    // Конструктор с дискретизацией функции source
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        logger.error("LinkedListTabulatedFunction: count < 2");
        if (count < 2) {
            throw new IllegalArgumentException("Кол-во точек < 2");
        }
        if (xFrom > xTo) {
            double tmp = xFrom;
            xFrom = xTo;
            xTo = tmp;
            logger.debug("LinkedListTabulatedFunction: поменяли границы местами");
        }
        double step = (xTo - xFrom) / (count - 1);
        for (int i = 0; i < count; i++) {
            double x = xFrom + step * i;
            double y = source.apply(x);
            addNode(x, y);
            logger.trace("LinkedListTabulatedFunction(source): i={}, x={}, y={}", i, x, y);
        }
        logger.info("LinkedListTabulatedFunction (from source) создана: [{}..{}], count={}", xFrom, xTo, count);
    }

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
        logger.trace("addNode: добавлен узел x={}, y={}, count={}", x, y, count);
    }

    // Получение узла по индексу
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            logger.error("getNode: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        Node current;
        if (index < count / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
                logger.trace("getNode: движение вперёд до i={}", i);
            }
        } else {
            current = head.prev;
            for (int i = count - 1; i > index; i--) {
                current = current.prev;
                logger.trace("getNode: движение назад до i={}", i);
            }
        }
        logger.trace("getNode: найден узел index={}, x={}, y={}", index, current.x, current.y);
        return current;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) { // Или лучше делать? "index >= getCount()"?
            logger.error("getX: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        logger.trace("getX: index={}, x={}", index, getNode(index).x);
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            logger.error("getY: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        logger.trace("getY: index={}, y={}", index, getNode(index).y);
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            logger.error("setY: некорректный индекс {}", index);
            throw new IllegalArgumentException("Некорректный индекс: " + index);
        }
        logger.debug("setY: индекс {} значение {} -> {}", index, getNode(index).y, value);
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x == x) {
                logger.trace("indexOfX: найден x={} на индексе {}", x, i);
                return i;
            }
            current = current.next;
        }
        logger.trace("indexOfX: x={} не найден", x);
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.y == y) {
                logger.trace("indexOfY: найден y={} на индексе {}", y, i);
                return i;
            }
            current = current.next;
        }
        logger.trace("indexOfY: y={} не найден", y);
        return -1;
    }

    @Override
    public double leftBound() {
        logger.trace("leftBound: {}", head.x);
        return head.x;
    }

    @Override
    public double rightBound() {
        logger.trace("rightBound: {}", head.prev.x);
        return head.prev.x;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (x < leftBound()) {
            logger.error("floorIndexOfX: x={} < leftBound {}", x, leftBound());
            throw new IllegalArgumentException("x: " + x + " < левой границы");
        }
        if (x >= rightBound()) {
            logger.trace("floorIndexOfX: x >= rightBound -> {}", count - 1);
            return count - 1;
        }

        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (current.x <= x && current.next.x > x) {
                logger.trace("floorIndexOfX: x={} -> {}", x, i);
                return i;
            }
            current = current.next;
        }
        logger.trace("floorIndexOfX: fallback {}", count - 1);
        return count - 1; // на всякий случай
    }

    @Override
    protected double extrapolateLeft(double x) {
        Node first = head;
        Node second = head.next;
        logger.debug("extrapolateLeft: x={} -> {}", x, interpolate(x, first.x, second.x, first.y, second.y));
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        Node last = head.prev;
        Node beforeLast = last.prev;
        logger.debug("extrapolateRight: x={} -> {}", x, interpolate(x, beforeLast.x, last.x, beforeLast.y, last.y));
        return interpolate(x, beforeLast.x, last.x, beforeLast.y, last.y);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        Node left = getNode(floorIndex);
        Node right = left.next;
        if (x < left.x || x > right.x) {
            logger.error("interpolate: x={} вне интервала [{},{}]", x, left.x, right.x);
            throw new ru.ssau.tk.avokado.lab2.exceptions.InterpolationException(
                    "x = " + x + " вне интервала интерполяции [" + left.x + ", " + right.x + "]"
            );
        }
        logger.debug("interpolate: x={}, [{},{}] -> {}", x, left.x, right.x, interpolate(x, left.x, right.x, left.y, right.y));
        return interpolate(x, left.x, right.x, left.y, right.y);
    }

    @Override
    public void insert(double x, double y) {
        if (head == null) {
            addNode(x, y);
            logger.info("insert: вставлен первый узел x={}, y={}", x, y);
            return;
        }

        Node current = head;
        do {
            if (current.x == x) {
                logger.debug("insert: x={} найден, заменяем y на {}", x, y);
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
                    logger.trace("insert: вставлен перед текущей головой, head обновлён");
                }
                count++;
                logger.info("insert: вставлен x={}, y={}, в позицию до x={}", x, y, current.x);
                return;
            }
            current = current.next;
        } while (current != head);

        // Если дошли до конца - вставляем в конец
        addNode(x, y);
        logger.info("insert: вставлен в конец x={}, y={}", x, y);
    }

    @Override
    public void remove(int index) {
        Node nodeToRemove = getNode(index);

        if (count == 1) {
            head = null;
            logger.info("remove: удалён единственный узел, список пуст");
        } else {
            nodeToRemove.prev.next = nodeToRemove.next;
            nodeToRemove.next.prev = nodeToRemove.prev;
            if (nodeToRemove == head) {
                head = head.next;
                logger.trace("remove: удалён head, head сдвинут");
            }
        }
        count--;
        logger.debug("remove: новый count={}", count);
    }

    @Override
    public Iterator<Point> iterator() {
        logger.trace("iterator: создан итератор по LinkedListTabulatedFunction");
        return new java.util.Iterator<>() {
            private Node current = head;
            private int iteratedCount = 0;

            @Override
            public boolean hasNext() {
                logger.trace("iterator.hasNext -> {}", iteratedCount < count);
                return iteratedCount < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    logger.trace("iterator.next: NoSuchElementException");
                    throw new java.util.NoSuchElementException();
                }
                Point point = new Point(current.x, current.y);
                logger.trace("iterator.next: возвращаем ({}, {})", point.x(), point.y());
                current = current.next;
                iteratedCount++;
                return point;
            }
        };
    }

    // Внутренний класс узла списка
    private static class Node implements Serializable {
        @Serial
        private static final long serialVersionUID = -3158113372194908641L;

        public Node next;
        public Node prev;
        public double x;
        public double y;

        public Node(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
