package ru.ssau.tk.avokado.lab2.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.User;

import java.util.List;
import java.util.Optional;

public interface FunctionRepository extends JpaRepository<FunctionEntity, Long> {
    Optional<FunctionEntity> findByName(String name);

    Page<FunctionEntity> findByUser(User user, Pageable pageable);
    List<FunctionEntity> findByUser(User user);

    Page<FunctionEntity> findByNameContaining(String name, Pageable pageable);

    long countByUser(User user);
}
