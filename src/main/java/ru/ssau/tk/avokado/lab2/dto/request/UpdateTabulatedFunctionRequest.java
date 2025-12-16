package ru.ssau.tk.avokado.lab2.dto.request;

public class UpdateTabulatedFunctionRequest {
    private Double xVal;
    private Double yVal;

    // Конструкторы
    public UpdateTabulatedFunctionRequest() {
    }

    public UpdateTabulatedFunctionRequest(Double xVal, Double yVal) {
        this.xVal = xVal;
        this.yVal = yVal;
    }

    // Геттеры и сеттеры
    public Double getXVal() {
        return xVal;
    }

    public void setXVal(Double xVal) {
        this.xVal = xVal;
    }

    public Double getYVal() {
        return yVal;
    }

    public void setYVal(Double yVal) {
        this.yVal = yVal;
    }
}