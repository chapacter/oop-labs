package ru.ssau.tk.avokado.lab2.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchCriteria {
    private final List<Condition> conditions = new ArrayList<>();
    private String sortField;
    private SortDirection sortDirection = SortDirection.ASC;
    private Integer limit;
    private Integer offset;

    // Builder methods
    public SearchCriteria addCondition(String field, Operator operator, Object value) {
        conditions.add(new Condition(field, operator, value));
        return this;
    }

    public SearchCriteria sortBy(String field, SortDirection direction) {
        this.sortField = field;
        this.sortDirection = direction;
        return this;
    }

    public SearchCriteria paginate(Integer limit, Integer offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    // Getters
    public List<Condition> getConditions() {
        return conditions;
    }

    public String getSortField() {
        return sortField;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public enum SortDirection {
        ASC, DESC
    }

    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL,
        LIKE,
        IN,
        BETWEEN,
        IS_NULL,
        IS_NOT_NULL
    }

    public record Condition(String field, Operator operator, Object value) {
    }
}
