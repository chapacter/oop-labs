package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.ProcessedFunction;
import java.util.List;
import java.util.Optional;

public interface ProcessedFunctionDao {
    ProcessedFunction create(ProcessedFunction pf);
    Optional<ProcessedFunction> findById(Long id);
    List<ProcessedFunction> findByOriginalFunctionId(Long originalId);
    List<ProcessedFunction> findByOperationId(Long operationId);
    List<ProcessedFunction> findAll();
    ProcessedFunction update(ProcessedFunction pf);
    void deleteById(Long id);
}