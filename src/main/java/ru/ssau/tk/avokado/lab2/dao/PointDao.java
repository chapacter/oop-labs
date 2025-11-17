package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.Point;

import java.util.List;
import java.util.Optional;

public interface PointDao {
    Point create(Point point);
    Optional<Point> findById(Long id);
    List<Point> findByFunctionId(Long functionId);
    List<Point> findAll();
    Point update(Point point);
    void deleteById(Long id);
    void deleteByFunctionId(Long functionId);
}