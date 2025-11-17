package ru.ssau.tk.avokado.lab2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import java.util.Optional;

public interface FunctionRepository extends JpaRepository<FunctionEntity, Long> {
    Optional<FunctionEntity> findByName(String name);
}
