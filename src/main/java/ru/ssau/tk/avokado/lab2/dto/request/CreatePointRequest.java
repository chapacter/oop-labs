package ru.ssau.tk.avokado.lab2.dto.request;

public class CreatePointRequest {
    private Long functionId;
    private Integer indexInFunction;
    private Double x;
    private Double y;

    // Конструкторы
    public CreatePointRequest() {
    }

    public CreatePointRequest(Long functionId, Integer indexInFunction, Double x, Double y) {
        this.functionId = functionId;
        this.indexInFunction = indexInFunction;
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

    public Integer getIndexInFunction() {
        return indexInFunction;
    }

    public void setIndexInFunction(Integer indexInFunction) {
        this.indexInFunction = indexInFunction;
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