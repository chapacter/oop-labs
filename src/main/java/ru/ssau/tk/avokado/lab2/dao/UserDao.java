package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.UserDto;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<UserDto> findById(Long id);
    Optional<UserDto> findByName(String name);
    List<UserDto> findAll();
    Optional<UserDto> findByNameAndPassword(String name, String passwordHash);
    Long save(UserDto user);
    boolean update(UserDto user);
    boolean updateName(Long id, String name);
    boolean updateAccessLvl(Long id, Integer accessLvl);
    boolean updatePasswordHash(Long id, String passwordHash);
    boolean delete(Long id);
    boolean existsByName(String name);
    List<UserDto> getUserStatistics(Long userId);
}
