package ru.ssau.tk.avokado.lab2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.ssau.tk.avokado.lab2.auth.Role;
import ru.ssau.tk.avokado.lab2.dto.CreateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.UpdateUserRequest;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;
import ru.ssau.tk.avokado.lab2.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserDto> list(String nameFilter, Pageable pageable) {
        logger.info("UserService.list nameFilter='{}' page={} size={}", nameFilter, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByNameContaining(nameFilter == null ? "" : nameFilter, pageable)
                .map(this::toDto);
    }

    @Override
    public UserDto get(Long id) {
        logger.info("UserService.get id={}", id);
        return userRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    @Override
    @Transactional
    public UserDto create(CreateUserRequest req) {
        logger.info("UserService.create name={}", req.getName());
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        Optional<User> exists = userRepository.findByName(req.getName());
        if (exists.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with same name already exists");
        }
        User u = new User();
        u.setName(req.getName());
        u.setAccessLvl(req.getAccessLvl() == null ? 1 : req.getAccessLvl());
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
        }
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        // default role
        u.getRoles().add(Role.ROLE_USER);
        User saved = userRepository.save(u);
        logger.info("Created user id={} name={}", saved.getId(), saved.getName());
        return toDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UpdateUserRequest req) {
        logger.info("UserService.update id={} name={}", id, req.getName());
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        if (req.getName() != null && !req.getName().isBlank()) {
            // check uniqueness
            userRepository.findByName(req.getName()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Name in use");
                }
            });
            u.setName(req.getName());
        }
        if (req.getAccessLvl() != null) {
            u.setAccessLvl(req.getAccessLvl());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        User saved = userRepository.save(u);
        logger.info("Updated user id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        logger.info("UserService.delete id={}", id);
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void grantRole(Long userId, Role role) {
        logger.info("UserService.grantRole userId={} role={}", userId, role);
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        if (!u.getRoles().contains(role)) {
            u.getRoles().add(role);
            userRepository.save(u);
            logger.info("Granted role {} to user {}", role, userId);
        } else {
            logger.info("User {} already has role {}", userId, role);
        }
    }

    @Override
    @Transactional
    public void revokeRole(Long userId, Role role) {
        logger.info("UserService.revokeRole userId={} role={}", userId, role);
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        if (u.getRoles().contains(role)) {
            u.getRoles().remove(role);
            userRepository.save(u);
            logger.info("Revoked role {} from user {}", role, userId);
        } else {
            logger.info("User {} does not have role {}", userId, role);
        }
    }

    private UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setAccessLvl(u.getAccessLvl());
        // copy roles to strings or Role enum, here Role enum
        dto.setRoles(u.getRoles() == null ? Set.of() : u.getRoles().stream().collect(Collectors.toSet()));
        return dto;
    }
}
