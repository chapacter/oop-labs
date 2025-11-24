package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.TabulatedFuncDto;

import java.util.List;
import java.util.Optional;

public interface TabulatedFuncDao {
    Optional<TabulatedFuncDto> findById(Long id);
    List<TabulatedFuncDto> findAll();
    List<TabulatedFuncDto> findByFuncId(Long funcId);
    List<TabulatedFuncDto> findByXVal(Double xVal);
    List<TabulatedFuncDto> findByYVal(Double yVal);
    List<TabulatedFuncDto> findByXValBetween(Double x1, Double x2);
    List<TabulatedFuncDto> findByYValBetween(Double y1, Double y2);
    List<TabulatedFuncDto> findByFuncIdsIn(List<Long> funcIds);
    Long save(TabulatedFuncDto tabulatedFunc);
    boolean update(TabulatedFuncDto tabulatedFunc);
    boolean delete(Long id);
    boolean deleteByFuncId(Long funcId);
    int countByFuncId(Long funcId);
}