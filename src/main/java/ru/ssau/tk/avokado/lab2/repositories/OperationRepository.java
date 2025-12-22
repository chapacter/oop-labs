package ru.ssau.tk.avokado.lab2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ssau.tk.avokado.lab2.entities.OperationEntity;

import java.util.Optional;

public interface OperationRepository extends JpaRepository<OperationEntity, Long> {
    Optional<OperationEntity> findByName(String name);
}
