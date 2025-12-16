package ru.ssau.tk.avokado.lab2.dto.request;

public class CreateResultValueRequest {
    private Long processedFunctionId;
    private Integer pointIndex;
    private Double x;
    private Double y;

    // Конструкторы
    public CreateResultValueRequest() {
    }

    public CreateResultValueRequest(Long processedFunctionId, Integer pointIndex, Double x, Double y) {
        this.processedFunctionId = processedFunctionId;
        this.pointIndex = pointIndex;
        this.x = x;
        this.y = y;
    }

    // Геттеры и сеттеры
    public Long getProcessedFunctionId() {
        return processedFunctionId;
    }

    public void setProcessedFunctionId(Long processedFunctionId) {
        this.processedFunctionId = processedFunctionId;
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