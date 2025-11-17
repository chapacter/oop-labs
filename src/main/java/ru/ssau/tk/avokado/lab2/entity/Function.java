package ru.ssau.tk.avokado.lab2.entity;

import java.time.ZonedDateTime;

public class Function {
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private ZonedDateTime createdAt;

    public Function() {}
    public Function(String name, String description, Long userId) {
        this.name = name;
        this.description = description;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}