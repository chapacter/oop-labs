package ru.ssau.tk.avokado.lab2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ssau.tk.avokado.lab2.dto.CreateFunctionRequest;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.UpdateFunctionRequest;

import java.util.List;

public interface FunctionService {
    Page<FunctionDto> list(String nameFilter, Long userId, boolean withPoints, Pageable pageable);

    FunctionDto get(Long id, boolean withPoints);

    FunctionDto create(CreateFunctionRequest req);

    FunctionDto update(Long id, UpdateFunctionRequest req);

    void delete(Long id);

    List<FunctionDto> findByUserId(Long userId);
}
