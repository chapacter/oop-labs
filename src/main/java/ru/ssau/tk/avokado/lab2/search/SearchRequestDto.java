package ru.ssau.tk.avokado.lab2.search;

public class SearchRequestDto {
    private String start = "user"; // user | function | point
    private Mode mode = Mode.BFS;
    private String field;
    private String value;
    private int limit = 100;
    private String sortBy;
    private boolean asc = true;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public enum Mode {BFS, DFS}
}
