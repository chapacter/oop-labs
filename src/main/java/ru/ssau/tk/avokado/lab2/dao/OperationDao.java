package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.OperationDto;

import java.util.List;
import java.util.Optional;

public interface OperationDao {
    Optional<OperationDto> findById(Long id);

    List<OperationDto> findAll();

    List<OperationDto> findByName(String name);

    List<OperationDto> findByNameContaining(String name);

    List<OperationDto> findByDescriptionLike(String descriptionPattern);

    Long save(OperationDto operation);

    boolean update(OperationDto operation);

    boolean updateName(Long id, String name);

    boolean updateDescription(Long id, String description);

    boolean delete(Long id);

    boolean existsByName(String name);
}