package ru.ssau.tk.avokado.lab2.dto.request;

public class CreateTabulatedFunctionRequest {
    private Long funcId;
    private Double xVal;
    private Double yVal;

    // Конструкторы
    public CreateTabulatedFunctionRequest() {
    }

    public CreateTabulatedFunctionRequest(Long funcId, Double xVal, Double yVal) {
        this.funcId = funcId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    // Геттеры и сеттеры
    public Long getFuncId() {
        return funcId;
    }

    public void setFuncId(Long funcId) {
        this.funcId = funcId;
    }

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