package ru.ssau.tk.avokado.lab2.entity;

public class ResultValue {
    private Long id;
    private Long processedFunctionId;
    private String key;
    private Double value;
    private String valueType;

    public ResultValue() {}
    public ResultValue(Long processedFunctionId, String key, Double value, String valueType) {
        this.processedFunctionId = processedFunctionId;
        this.key = key;
        this.value = value;
        this.valueType = valueType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProcessedFunctionId() { return processedFunctionId; }
    public void setProcessedFunctionId(Long processedFunctionId) { this.processedFunctionId = processedFunctionId; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }
}