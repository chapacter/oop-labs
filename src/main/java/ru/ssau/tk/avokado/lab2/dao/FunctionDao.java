package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.PointDto;
import java.util.List;
import java.util.Optional;

public interface FunctionDao {
    Optional<FunctionDto> findById(Long id);
    List<FunctionDto> findByUserId(Long userId);
    List<FunctionDto> findByName(String name);
    List<FunctionDto> findByNameLike(String namePattern);
    List<FunctionDto> findByUserIdAndNameLikeAndCreatedAtBetween(Long userId, String namePattern, java.time.ZonedDateTime start, java.time.ZonedDateTime end);
    List<FunctionDto> findByUserIdOrderByNameAsc(Long userId);
    Long save(FunctionDto function);
    boolean update(FunctionDto function);
    boolean updateName(Long id, String name);
    boolean updateFormat(Long id, Integer format);
    boolean updateFuncResult(Long id, String funcResult);
    boolean delete(Long id);

    List<PointDto> findPointsByFunctionId(Long functionId);
    Optional<PointDto> findPointByFunctionIdAndPointIndex(Long functionId, Integer pointIndex);
    void savePoints(Long functionId, List<PointDto> points);
    boolean updatePoint(Long functionId, PointDto point);
    boolean deletePoint(Long pointId);
    boolean deleteAllPointsByFunctionId(Long functionId);
    int countPointsByFunctionId(Long functionId);
}
