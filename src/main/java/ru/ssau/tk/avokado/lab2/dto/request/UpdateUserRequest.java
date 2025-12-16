package ru.ssau.tk.avokado.lab2.dto.request;

public class UpdateUserRequest {
    private String name;
    private Integer accessLvl;

    // Конструкторы
    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String name, Integer accessLvl) {
        this.name = name;
        this.accessLvl = accessLvl;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAccessLvl() {
        return accessLvl;
    }

    public void setAccessLvl(Integer accessLvl) {
        this.accessLvl = accessLvl;
    }
}