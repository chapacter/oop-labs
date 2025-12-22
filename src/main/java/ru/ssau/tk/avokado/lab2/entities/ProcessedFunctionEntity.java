package ru.ssau.tk.avokado.lab2.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "processed_functions")
public class ProcessedFunctionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private OperationEntity operation;

    @Column(name = "result_summary", length = 2048)
    private String resultSummary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "processedFunction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultValueEntity> resultValues;

    public ProcessedFunctionEntity() {
    }

    public ProcessedFunctionEntity(FunctionEntity function, OperationEntity operation, String resultSummary) {
        this.function = function;
        this.operation = operation;
        this.resultSummary = resultSummary;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FunctionEntity getFunction() {
        return function;
    }

    public void setFunction(FunctionEntity function) {
        this.function = function;
    }

    public OperationEntity getOperation() {
        return operation;
    }

    public void setOperation(OperationEntity operation) {
        this.operation = operation;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<ResultValueEntity> getResultValues() {
        return resultValues;
    }

    public void setResultValues(List<ResultValueEntity> resultValues) {
        this.resultValues = resultValues;
    }
}
