package ru.ssau.tk.avokado.lab2.entity;

public class Point {
    private Long id;
    private Long functionId;
    private Double xValue;  // DECIMAL -> Double
    private Double yValue;
    private Integer pointOrder;

    public Point() {}
    public Point(Long functionId, Double xValue, Double yValue, Integer pointOrder) {
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
        this.pointOrder = pointOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public Double getXValue() { return xValue; }
    public void setXValue(Double xValue) { this.xValue = xValue; }
    public Double getYValue() { return yValue; }
    public void setYValue(Double yValue) { this.yValue = yValue; }
    public Integer getPointOrder() { return pointOrder; }
    public void setPointOrder(Integer pointOrder) { this.pointOrder = pointOrder; }
}