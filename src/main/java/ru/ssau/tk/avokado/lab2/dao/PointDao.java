package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.PointDto;

import java.util.List;
import java.util.Optional;

public interface PointDao {
    Optional<PointDto> findById(Long id);

    List<PointDto> findByFunctionId(Long functionId);

    List<PointDto> findByX(Double x);

    List<PointDto> findByY(Double y);

    List<PointDto> findByXBetween(Double x1, Double x2);

    List<PointDto> findByYBetween(Double y1, Double y2);

    List<PointDto> findByFunctionIdsIn(List<Long> functionIds);

    List<PointDto> findByFunctionIdOrderByXAsc(Long functionId);

    List<PointDto> findByFunctionIdOrderByYAsc(Long functionId);

    List<PointDto> findByFunctionIdOrderByXDesc(Long functionId);

    List<PointDto> findByFunctionIdOrderByYDesc(Long functionId);

    List<PointDto> findByUserId(Long userId);

    Optional<PointDto> findByFunctionIdAndPointIndex(Long functionId, Integer pointIndex);

    Long save(PointDto point);

    boolean update(PointDto point);

    boolean delete(Long id);

    boolean deleteByFunctionId(Long functionId);

    int countByFunctionId(Long functionId);

    List<PointDto> findAll();
}
