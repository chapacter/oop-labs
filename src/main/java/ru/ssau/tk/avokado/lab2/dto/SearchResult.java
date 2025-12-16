package ru.ssau.tk.avokado.lab2.dto;

import java.util.List;

public record SearchResult<T>(List<T> items, int totalCount, int page, int pageSize) {

    public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    public boolean hasNext() {
        return page * pageSize < totalCount;
    }

    public boolean hasPrevious() {
        return page > 1;
    }
}
