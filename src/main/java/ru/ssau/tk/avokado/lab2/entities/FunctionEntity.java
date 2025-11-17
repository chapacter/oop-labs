package ru.ssau.tk.avokado.lab2.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "functions")
public class FunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private Integer format;

    @Column(name = "func_result", length = 2048)
    private String funcResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TabulatedPoint> points;

    public FunctionEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFormat() {
        return format;
    }

    public void setFormat(Integer format) {
        this.format = format;
    }

    public String getFuncResult() {
        return funcResult;
    }

    public void setFuncResult(String funcResult) {
        this.funcResult = funcResult;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<TabulatedPoint> getPoints() {
        return points;
    }

    public void setPoints(List<TabulatedPoint> points) {
        this.points = points;
    }
}
