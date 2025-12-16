package ru.ssau.tk.avokado.lab2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.avokado.lab2.auth.Role;
import ru.ssau.tk.avokado.lab2.dto.CreateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public UserDto register(@RequestBody CreateUserRequest req) {
        return userService.create(req);
    }
}
