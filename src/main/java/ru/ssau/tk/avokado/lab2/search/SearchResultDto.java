package ru.ssau.tk.avokado.lab2.search;

public class SearchResultDto {
    private String type;
    private Object id;
    private Object entity;

    public SearchResultDto() {
    }

    public SearchResultDto(String type, Object id, Object entity) {
        this.type = type;
        this.id = id;
        this.entity = entity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}
