package ru.ssau.tk.avokado.lab2.dto.request;

/**
 * Универсальный класс запроса для создания/обновления сущностей
 */
public class SimpleEntityRequest {
    private String name;
    private String value;
    private Double xVal;
    private Double yVal;
    private Long referenceId1;
    private Long referenceId2;
    private Integer intValue;

    // Конструкторы
    public SimpleEntityRequest() {
    }

    public SimpleEntityRequest(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public SimpleEntityRequest(String name, Double xVal, Double yVal) {
        this.name = name;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    // Getters and setters
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

    public Double getXVal() {
        return xVal;
    }

    public void setXVal(Double xVal) {
        this.xVal = xVal;
    }

    public Double getYVal() {
        return yVal;
    }

    public void setYVal(Double yVal) {
        this.yVal = yVal;
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
}