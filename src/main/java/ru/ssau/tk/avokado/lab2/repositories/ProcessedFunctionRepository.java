package ru.ssau.tk.avokado.lab2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ssau.tk.avokado.lab2.entities.ProcessedFunctionEntity;

public interface ProcessedFunctionRepository extends JpaRepository<ProcessedFunctionEntity, Long> {
}
