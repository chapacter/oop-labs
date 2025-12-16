package ru.ssau.tk.avokado.lab2.dto.request;

public class CreateFunctionRequest {
    private String name;
    private Integer format;
    private Long userId;
    private String funcResult;

    // Конструкторы
    public CreateFunctionRequest() {
    }

    public CreateFunctionRequest(String name, Integer format, Long userId, String funcResult) {
        this.name = name;
        this.format = format;
        this.userId = userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFuncResult() {
        return funcResult;
    }

    public void setFuncResult(String funcResult) {
        this.funcResult = funcResult;
    }
}