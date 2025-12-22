package ru.ssau.tk.avokado.lab2.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;

import java.util.List;

public interface PointRepository extends JpaRepository<TabulatedPoint, Long> {
    List<TabulatedPoint> findByFunction(FunctionEntity function);

    Page<TabulatedPoint> findByFunction(FunctionEntity function, Pageable pageable);

    long countByFunction(FunctionEntity function);
}
