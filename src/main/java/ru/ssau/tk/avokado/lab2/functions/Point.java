package ru.ssau.tk.avokado.lab2.functions;
public final class Point {
    public final double x;
    public final double y;

    public Point (double x,double y){
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}