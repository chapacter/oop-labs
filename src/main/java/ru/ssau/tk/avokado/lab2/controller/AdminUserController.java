package ru.ssau.tk.avokado.lab2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.avokado.lab2.auth.Role;
import ru.ssau.tk.avokado.lab2.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) { this.userService = userService; }

    // Grant role to user (admin only)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/roles")
    public void addRole(@PathVariable Long id, @RequestParam Role role) {
        userService.grantRole(id, role);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}/roles")
    public void removeRole(@PathVariable Long id, @RequestParam Role role) {
        userService.revokeRole(id, role);
    }
}
