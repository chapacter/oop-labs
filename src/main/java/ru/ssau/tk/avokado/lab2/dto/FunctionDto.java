package ru.ssau.tk.avokado.lab2.dto;

import java.time.ZonedDateTime;

public class FunctionDto {
    private Long id;
    private Long userId;
    private String name;
    private Integer format;
    private String funcResult;
    private ZonedDateTime createdAt;

    public FunctionDto(Long id, Long userId, String name, Integer format, String funcResult) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.format = format;
        this.funcResult = funcResult;
    }

    public FunctionDto(Long userId, String name, Integer format, String funcResult) {
        this.userId = userId;
        this.name = name;
        this.format = format;
        this.funcResult = funcResult;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getFormat() { return format; }
    public void setFormat(Integer format) { this.format = format; }

    public String getFuncResult() { return funcResult; }
    public void setFuncResult(String funcResult) { this.funcResult = funcResult; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
