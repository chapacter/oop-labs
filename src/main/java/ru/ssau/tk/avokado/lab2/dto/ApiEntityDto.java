package ru.ssau.tk.avokado.lab2.dto;

/**
 * Обобщенный DTO класс для представления сущностей API
 */
public class ApiEntityDto {
    private Long id;
    private String type; // Тип сущности (user, function, point, operation и т.д.)
    private Object data; // Данные сущности
    private Long parentId; // ID родительской сущности (если применимо)
    private Long relatedId; // ID связанной сущности (если применимо)

    public ApiEntityDto() {
    }

    public ApiEntityDto(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public ApiEntityDto(Long id, String type, Object data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }

    public ApiEntityDto(Long id, String type, Object data, Long parentId, Long relatedId) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.parentId = parentId;
        this.relatedId = relatedId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }
}