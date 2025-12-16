package ru.ssau.tk.avokado.lab2.dto.request;

public class UpdateProcessedFunctionRequest {
    private String resultSummary;

    // Конструкторы
    public UpdateProcessedFunctionRequest() {
    }

    public UpdateProcessedFunctionRequest(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    // Геттеры и сеттеры
    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }
}