package ru.ssau.tk.avokado.lab2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;

import java.util.List;

public interface PointRepository extends JpaRepository<TabulatedPoint, Long> {
    List<TabulatedPoint> findByFunction(FunctionEntity function);
}
