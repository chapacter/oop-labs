package ru.ssau.tk.avokado.lab2.dto;

public class TabulatedFuncDto {
    private Long id;
    private Long funcId;
    private Double xVal;
    private Double yVal;

    public TabulatedFuncDto() {
    }

    public TabulatedFuncDto(Long funcId, Double xVal, Double yVal) {
        this.funcId = funcId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public TabulatedFuncDto(Long id, Long funcId, Double xVal, Double yVal) {
        this.id = id;
        this.funcId = funcId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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