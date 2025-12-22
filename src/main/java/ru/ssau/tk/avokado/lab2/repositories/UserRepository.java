package ru.ssau.tk.avokado.lab2.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ssau.tk.avokado.lab2.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByNameContaining(String name, Pageable pageable);
    Optional<User> findByName(String name);
}

