package ru.ssau.tk.avokado.lab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.avokado.lab2.dto.CreateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.UpdateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UserDto> list(@RequestParam(required = false) String name, Pageable pageable) {
        logger.info("GET /api/users name={}, page={}, size={}, sort={}", name,
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return service.list(name, pageable);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        logger.info("GET /api/users/{}", id);
        return service.get(id);
    }

    @PostMapping
    public UserDto create(@RequestBody CreateUserRequest req) {
        logger.info("POST /api/users create name={}", req.getName());
        return service.create(req);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        logger.info("PUT /api/users/{} update name={}", id, req.getName());
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        logger.info("DELETE /api/users/{}", id);
        service.delete(id);
    }
}