package ru.ssau.tk.avokado.lab2.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "points")
public class TabulatedPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_index")
    private Integer indexInFunction;

    private double x;
    private double y;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;

    public TabulatedPoint() { }

    public TabulatedPoint(Integer indexInFunction, double x, double y, FunctionEntity function) {
        this.indexInFunction = indexInFunction;
        this.x = x;
        this.y = y;
        this.function = function;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getIndexInFunction() { return indexInFunction; }
    public void setIndexInFunction(Integer indexInFunction) { this.indexInFunction = indexInFunction; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public FunctionEntity getFunction() { return function; }
    public void setFunction(FunctionEntity function) { this.function = function; }
}
