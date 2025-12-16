package ru.ssau.tk.avokado.lab2.dto.request;

public class CreateProcessedFunctionRequest {
    private Long functionId;
    private Long operationId;
    private String resultSummary;

    // Конструкторы
    public CreateProcessedFunctionRequest() {
    }

    public CreateProcessedFunctionRequest(Long functionId, Long operationId, String resultSummary) {
        this.functionId = functionId;
        this.operationId = operationId;
        this.resultSummary = resultSummary;
    }

    // Геттеры и сеттеры
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
}