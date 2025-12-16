package ru.ssau.tk.avokado.lab2.dto.request;

public class UpdateFunctionRequest {
    private String name;
    private Integer format;
    private String funcResult;

    // Конструкторы
    public UpdateFunctionRequest() {
    }

    public UpdateFunctionRequest(String name, Integer format, String funcResult) {
        this.name = name;
        this.format = format;
        this.funcResult = funcResult;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFormat() {
        return format;
    }

    public void setFormat(Integer format) {
        this.format = format;
    }

    public String getFuncResult() {
        return funcResult;
    }

    public void setFuncResult(String funcResult) {
        this.funcResult = funcResult;
    }
}