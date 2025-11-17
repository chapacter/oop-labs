package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.ResultValue;
import java.util.List;
import java.util.Optional;

public interface ResultValueDao {
    ResultValue create(ResultValue rv);
    Optional<ResultValue> findById(Long id);
    List<ResultValue> findByProcessedFunctionId(Long processedId);
    List<ResultValue> findAll();
    ResultValue update(ResultValue rv);
    void deleteById(Long id);
}