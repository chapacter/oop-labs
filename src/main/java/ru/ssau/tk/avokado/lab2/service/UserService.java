package ru.ssau.tk.avokado.lab2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ssau.tk.avokado.lab2.auth.Role;
import ru.ssau.tk.avokado.lab2.dto.CreateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.UpdateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.UserDto;

public interface UserService {
    Page<UserDto> list(String nameFilter, Pageable pageable);
    UserDto get(Long id);
    UserDto create(CreateUserRequest req);
    UserDto update(Long id, UpdateUserRequest req);
    void delete(Long id);
    void grantRole(Long userId, Role role);
    void revokeRole(Long userId, Role role);
}
