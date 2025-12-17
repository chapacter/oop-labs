package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface UserRoleDao {
    boolean addUserRole(Long userId, String role);

    boolean removeUserRole(Long userId, String role);

    Set<String> getUserRoles(Long userId);

    boolean userHasRole(Long userId, String role);

    List<UserDto> getUsersByRole(String role);

    boolean deleteAllUserRoles(Long userId);
}