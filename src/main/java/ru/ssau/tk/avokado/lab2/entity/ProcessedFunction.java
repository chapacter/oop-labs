package ru.ssau.tk.avokado.lab2.entity;

import java.time.ZonedDateTime;

public class ProcessedFunction {
    private Long id;
    private Long originalFunctionId;
    private Long operationId;
    private Long resultingFunctionId;
    private String parameters;
    private ZonedDateTime processedAt;

    public ProcessedFunction() {}
    public ProcessedFunction(Long originalFunctionId, Long operationId, Long resultingFunctionId, String parameters) {
        this.originalFunctionId = originalFunctionId;
        this.operationId = operationId;
        this.resultingFunctionId = resultingFunctionId;
        this.parameters = parameters;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOriginalFunctionId() { return originalFunctionId; }
    public void setOriginalFunctionId(Long originalFunctionId) { this.originalFunctionId = originalFunctionId; }
    public Long getOperationId() { return operationId; }
    public void setOperationId(Long operationId) { this.operationId = operationId; }
    public Long getResultingFunctionId() { return resultingFunctionId; }
    public void setResultingFunctionId(Long resultingFunctionId) { this.resultingFunctionId = resultingFunctionId; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public ZonedDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(ZonedDateTime processedAt) { this.processedAt = processedAt; }
}