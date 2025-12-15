package ru.ssau.tk.avokado.lab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.avokado.lab2.dto.*;
import ru.ssau.tk.avokado.lab2.service.FunctionService;

import java.util.List;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {
    private final Logger logger = LoggerFactory.getLogger(FunctionController.class);
    private final FunctionService service;

    public FunctionController(FunctionService service) { this.service = service; }

    @GetMapping
    public org.springframework.data.domain.Page<FunctionDto> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "false") boolean withPoints,
            Pageable pageable) {
        logger.info("GET /api/functions name={}, userId={}, withPoints={}, page={}, size={}, sort={}",
                name, userId, withPoints, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return service.list(name, userId, withPoints, pageable);
    }

    @GetMapping("/{id}")
    public FunctionDto get(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean withPoints) {
        logger.info("GET /api/functions/{} withPoints={}", id, withPoints);
        return service.get(id, withPoints);
    }

    @PostMapping
    public FunctionDto create(@RequestBody CreateFunctionRequest req) {
        logger.info("POST /api/functions create name={}, userId={}", req.name(), req.userId());
        return service.create(req);
    }

    @PutMapping("/{id}")
    public FunctionDto update(@PathVariable Long id, @RequestBody UpdateFunctionRequest req) {
        logger.info("PUT /api/functions/{} update", id);
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        logger.info("DELETE /api/functions/{}", id);
        service.delete(id);
    }

    @GetMapping("/by-user/{userId}")
    public List<FunctionDto> byUser(@PathVariable Long userId) {
        logger.info("GET /api/functions/by-user/{}", userId);
        return service.findByUserId(userId);
    }
}
