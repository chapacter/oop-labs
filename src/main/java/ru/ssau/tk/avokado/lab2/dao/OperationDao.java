package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.Operation;
import java.util.List;
import java.util.Optional;

public interface OperationDao {
    Operation create(Operation operation);
    Optional<Operation> findById(Long id);
    Optional<Operation> findByName(String name);
    List<Operation> findAll();
    Operation update(Operation operation);
    void deleteById(Long id);
}