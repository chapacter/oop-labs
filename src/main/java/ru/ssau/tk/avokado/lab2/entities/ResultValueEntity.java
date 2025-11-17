package ru.ssau.tk.avokado.lab2.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "result_values")
public class ResultValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_index")
    private Integer pointIndex;

    private Double x;
    private Double y;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_function_id", nullable = false)
    private ProcessedFunctionEntity processedFunction;

    public ResultValueEntity() { }

    public ResultValueEntity(Integer pointIndex, Double x, Double y, ProcessedFunctionEntity processedFunction) {
        this.pointIndex = pointIndex;
        this.x = x;
        this.y = y;
        this.processedFunction = processedFunction;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPointIndex() { return pointIndex; }
    public void setPointIndex(Integer pointIndex) { this.pointIndex = pointIndex; }

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public ProcessedFunctionEntity getProcessedFunction() { return processedFunction; }
    public void setProcessedFunction(ProcessedFunctionEntity processedFunction) { this.processedFunction = processedFunction; }
}
