package ru.ssau.tk.avokado.lab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.avokado.lab2.dto.CreatePointRequest;
import ru.ssau.tk.avokado.lab2.dto.PointDto;
import ru.ssau.tk.avokado.lab2.dto.UpdatePointRequest;
import ru.ssau.tk.avokado.lab2.service.PointService;

@RestController
@RequestMapping("/api/points")
public class PointController {
    private final Logger logger = LoggerFactory.getLogger(PointController.class);
    private final PointService service;

    public PointController(PointService service) {
        this.service = service;
    }

    @GetMapping
    public org.springframework.data.domain.Page<PointDto> list(
            @RequestParam(required = false) Long functionId, Pageable pageable) {
        logger.info("GET /api/points functionId={}, page={}, size={}, sort={}", functionId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return service.list(functionId, pageable);
    }

    @GetMapping("/{id}")
    public PointDto get(@PathVariable Long id) {
        logger.info("GET /api/points/{}", id);
        return service.get(id);
    }

    @PostMapping
    public PointDto create(@RequestBody CreatePointRequest req) {
        logger.info("POST /api/points create functionId={} index={}", req.functionId(), req.indexInFunction());
        return service.create(req);
    }

    @PutMapping("/{id}")
    public PointDto update(@PathVariable Long id, @RequestBody UpdatePointRequest req) {
        logger.info("PUT /api/points/{} update", id);
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        logger.info("DELETE /api/points/{}", id);
        service.delete(id);
    }
}
