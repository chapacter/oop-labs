package ru.ssau.tk.avokado.lab2.dto.request;

public class UpdatePointRequest {
    private Long functionId;
    private Integer pointIndex;
    private Double x;
    private Double y;

    // Конструкторы
    public UpdatePointRequest() {
    }

    public UpdatePointRequest(Long functionId, Integer pointIndex, Double x, Double y) {
        this.functionId = functionId;
        this.pointIndex = pointIndex;
        this.x = x;
        this.y = y;
    }

    // Геттеры и сеттеры
    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Integer getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(Integer pointIndex) {
        this.pointIndex = pointIndex;
    }

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