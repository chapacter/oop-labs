package ru.ssau.tk.avokado.lab2.functions;

public interface MathFunction extends MathFunction {
    int getCount(); // получение количества табулированных значений
    double getX(int index); // получение значения аргумента x по номеру индекса:
    double getY(int index); // получение y

    void setY(int index, double value); // задающий значение y по номеру индекса

    int indexOfX(double x); // Метод, возвращающий индекс аргумента x. Предполагается, что все x различны. Если такого x в таблице нет, то необходимо вернуть -1:
    int indexOfY(double y); // Для y

    double leftBound(); // Возвращающий самый правый x
    double rightBound(); // Самый левый


}

