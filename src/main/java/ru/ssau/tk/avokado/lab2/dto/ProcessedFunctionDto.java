package ru.ssau.tk.avokado.lab2.dto;

import java.time.ZonedDateTime;

public class ProcessedFunctionDto {
    private Long id;
    private Long functionId;
    private Long operationId;
    private String resultSummary;
    private ZonedDateTime createdAt;

    public ProcessedFunctionDto() {
    }

    public ProcessedFunctionDto(Long functionId, Long operationId, String resultSummary) {
        this.functionId = functionId;
        this.operationId = operationId;
        this.resultSummary = resultSummary;
        this.createdAt = ZonedDateTime.now();
    }

    public ProcessedFunctionDto(Long id, Long functionId, Long operationId, String resultSummary, ZonedDateTime createdAt) {
        this.id = id;
        this.functionId = functionId;
        this.operationId = operationId;
        this.resultSummary = resultSummary;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}