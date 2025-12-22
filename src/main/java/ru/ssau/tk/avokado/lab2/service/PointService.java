package ru.ssau.tk.avokado.lab2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ssau.tk.avokado.lab2.dto.CreatePointRequest;
import ru.ssau.tk.avokado.lab2.dto.PointDto;
import ru.ssau.tk.avokado.lab2.dto.UpdatePointRequest;

public interface PointService {
    Page<PointDto> list(Long functionId, Pageable pageable);

    PointDto get(Long id);

    PointDto create(CreatePointRequest req);

    PointDto update(Long id, UpdatePointRequest req);

    void delete(Long id);
}
