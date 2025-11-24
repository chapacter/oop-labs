package ru.ssau.tk.avokado.lab2.dto;

public class ResultValueDto {
    private Long id;
    private Long processedFunctionId;
    private Integer pointIndex;
    private Double x;
    private Double y;

    public ResultValueDto() {
    }

    public ResultValueDto(Long processedFunctionId, Integer pointIndex, Double x, Double y) {
        this.processedFunctionId = processedFunctionId;
        this.pointIndex = pointIndex;
        this.x = x;
        this.y = y;
    }

    public ResultValueDto(Long id, Long processedFunctionId, Integer pointIndex, Double x, Double y) {
        this.id = id;
        this.processedFunctionId = processedFunctionId;
        this.pointIndex = pointIndex;
        this.x = x;
        this.y = y;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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