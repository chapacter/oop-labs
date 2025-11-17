package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.Function;
import java.util.List;
import java.util.Optional;

public interface FunctionDao {
    Function create(Function function);
    Optional<Function> findById(Long id);
    List<Function> findByUserId(Long userId);
    List<Function> findAll();
    Function update(Function function);
    void deleteById(Long id);
}