package ru.ssau.tk.avokado.lab2.dto;

public class OperationDto {
    private Long id;
    private String name;
    private String description;

    public OperationDto() {
    }

    public OperationDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public OperationDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}