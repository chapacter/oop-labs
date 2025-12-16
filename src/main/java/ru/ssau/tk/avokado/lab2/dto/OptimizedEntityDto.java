package ru.ssau.tk.avokado.lab2.dto;

import java.time.ZonedDateTime;

/**
 * Оптимизированный DTO класс для универсального представления сущностей
 */
public class OptimizedEntityDto {
    private Long id;
    private EntityType entityType;
    private String name;
    private String value;
    private Double numericValue1;
    private Double numericValue2;
    private Long referenceId1;
    private Long referenceId2;
    private Integer intValue;
    private ZonedDateTime createdAt;

    // Конструкторы
    public OptimizedEntityDto() {
    }

    public OptimizedEntityDto(EntityType entityType, String name) {
        this.entityType = entityType;
        this.name = name;
    }

    public OptimizedEntityDto(Long id, EntityType entityType, String name) {
        this.id = id;
        this.entityType = entityType;
        this.name = name;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Double getNumericValue1() {
        return numericValue1;
    }

    public void setNumericValue1(Double numericValue1) {
        this.numericValue1 = numericValue1;
    }

    public Double getNumericValue2() {
        return numericValue2;
    }

    public void setNumericValue2(Double numericValue2) {
        this.numericValue2 = numericValue2;
    }

    public Long getReferenceId1() {
        return referenceId1;
    }

    public void setReferenceId1(Long referenceId1) {
        this.referenceId1 = referenceId1;
    }

    public Long getReferenceId2() {
        return referenceId2;
    }

    public void setReferenceId2(Long referenceId2) {
        this.referenceId2 = referenceId2;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public enum EntityType {
        USER, FUNCTION, POINT, OPERATION, PROCESSED_FUNCTION, RESULT_VALUE, TABULATED_FUNCTION
    }
}