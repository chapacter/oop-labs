package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.ResultValueDto;

import java.util.List;
import java.util.Optional;

public interface ResultValueDao {
    Optional<ResultValueDto> findById(Long id);

    List<ResultValueDto> findAll();

    List<ResultValueDto> findByProcessedFunctionId(Long processedFunctionId);

    List<ResultValueDto> findByXBetween(Double x1, Double x2);

    List<ResultValueDto> findByYBetween(Double y1, Double y2);

    List<ResultValueDto> findByPointIndex(Integer pointIndex);

    List<ResultValueDto> findByProcessedFunctionIdsIn(List<Long> processedFunctionIds);

    Long save(ResultValueDto resultValue);

    boolean update(ResultValueDto resultValue);

    boolean delete(Long id);

    boolean deleteByProcessedFunctionId(Long processedFunctionId);

    int countByProcessedFunctionId(Long processedFunctionId);
}