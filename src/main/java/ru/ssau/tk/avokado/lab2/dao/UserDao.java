package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    User create(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    User update(User user);
    void deleteById(Long id);
}