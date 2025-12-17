package ru.ssau.tk.avokado.lab2.functions;

public record Point(double x, double y) {

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}