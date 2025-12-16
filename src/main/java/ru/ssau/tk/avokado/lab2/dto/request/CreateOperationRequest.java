package ru.ssau.tk.avokado.lab2.dto.request;

public class CreateOperationRequest {
    private String name;
    private String description;

    // Конструкторы
    public CreateOperationRequest() {
    }

    public CreateOperationRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Геттеры и сеттеры
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