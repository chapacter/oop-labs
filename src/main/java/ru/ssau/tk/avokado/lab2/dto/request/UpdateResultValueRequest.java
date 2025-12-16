package ru.ssau.tk.avokado.lab2.dto.request;

public class UpdateResultValueRequest {
    private Double x;
    private Double y;

    // Конструкторы
    public UpdateResultValueRequest() {
    }

    public UpdateResultValueRequest(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    // Геттеры и сеттеры
    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}