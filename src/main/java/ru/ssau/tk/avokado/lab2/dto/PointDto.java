package ru.ssau.tk.avokado.lab2.dto;

public class PointDto {
    private Long id;
    private Long functionId;
    private Double x;
    private Double y;
    private Integer pointIndex;

    public PointDto(Long functionId, Double x, Double y, Integer pointIndex) {
        this.functionId = functionId;
        this.x = x;
        this.y = y;
        this.pointIndex = pointIndex;
    }

    public PointDto(Long id, Long functionId, Double x, Double y, Integer pointIndex) {
        this.id = id;
        this.functionId = functionId;
        this.x = x;
        this.y = y;
        this.pointIndex = pointIndex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public Integer getPointIndex() { return pointIndex; }
    public void setPointIndex(Integer pointIndex) { this.pointIndex = pointIndex; }

    public Integer getIndex() {
        return pointIndex;
    }
}
