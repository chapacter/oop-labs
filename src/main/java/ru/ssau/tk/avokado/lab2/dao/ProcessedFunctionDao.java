package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.ProcessedFunctionDto;

import java.util.List;
import java.util.Optional;

public interface ProcessedFunctionDao {
    Optional<ProcessedFunctionDto> findById(Long id);

    List<ProcessedFunctionDto> findAll();

    List<ProcessedFunctionDto> findByFunctionId(Long functionId);

    List<ProcessedFunctionDto> findByOperationId(Long operationId);

    List<ProcessedFunctionDto> findByResultSummaryLike(String resultSummaryPattern);

    List<ProcessedFunctionDto> findByCreatedAtBetween(java.time.ZonedDateTime start, java.time.ZonedDateTime end);

    Long save(ProcessedFunctionDto processedFunction);

    boolean update(ProcessedFunctionDto processedFunction);

    boolean updateResultSummary(Long id, String resultSummary);

    boolean delete(Long id);

    boolean deleteByFunctionId(Long functionId);

    boolean deleteByOperationId(Long operationId);
}