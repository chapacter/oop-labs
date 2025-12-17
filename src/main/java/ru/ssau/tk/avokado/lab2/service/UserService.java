package ru.ssau.tk.avokado.lab2.service;

import ru.ssau.tk.avokado.lab2.dao.JdbcUserDao;
import ru.ssau.tk.avokado.lab2.dao.JdbcUserRoleDao;
import ru.ssau.tk.avokado.lab2.dao.UserDao;
import ru.ssau.tk.avokado.lab2.dao.UserRoleDao;
import ru.ssau.tk.avokado.lab2.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Сервисный слой для работы с пользователями
 */
public class UserService {
    private final UserDao userDao;
    private final UserRoleDao userRoleDao;

    public UserService() {
        this.userDao = new JdbcUserDao();
        this.userRoleDao = new JdbcUserRoleDao();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
        this.userRoleDao = new JdbcUserRoleDao();
    }

    public UserService(UserDao userDao, UserRoleDao userRoleDao) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
    }

    public Optional<UserDto> findById(Long id) {
        Optional<UserDto> user = userDao.findById(id);
        if (user.isPresent()) {
            UserDto userDto = user.get();
            Set<String> roles = userRoleDao.getUserRoles(id);
            userDto.setRoles(roles);
        }
        return user;
    }

    public List<UserDto> findAll() {
        List<UserDto> users = userDao.findAll();
        for (UserDto user : users) {
            Set<String> roles = userRoleDao.getUserRoles(user.getId());
            user.setRoles(roles);
        }
        return users;
    }

    public List<UserDto> findByNameContaining(String name) {
        List<UserDto> users = userDao.findByNameContaining(name);
        for (UserDto user : users) {
            Set<String> roles = userRoleDao.getUserRoles(user.getId());
            user.setRoles(roles);
        }
        return users;
    }

    public Long save(UserDto user) {
        Long userId = userDao.save(user);
        if (userId != null && user.getRoles() != null && !user.getRoles().isEmpty()) {
            for (String role : user.getRoles()) {
                userRoleDao.addUserRole(userId, role);
            }
        }
        return userId;
    }

    public boolean update(UserDto user) {
        boolean updated = userDao.update(user);
        if (updated && user.getRoles() != null) {
            // Удаляем все старые роли
            userRoleDao.deleteAllUserRoles(user.getId());
            // Добавляем новые роли
            for (String role : user.getRoles()) {
                userRoleDao.addUserRole(user.getId(), role);
            }
        }
        return updated;
    }

    public boolean delete(Long id) {
        // Удаляем сначала роли пользователя, затем самого пользователя
        userRoleDao.deleteAllUserRoles(id);
        return userDao.delete(id);
    }

    public boolean addRoleToUser(Long userId, String role) {
        return userRoleDao.addUserRole(userId, role);
    }

    public boolean removeRoleFromUser(Long userId, String role) {
        return userRoleDao.removeUserRole(userId, role);
    }

    public Set<String> getUserRoles(Long userId) {
        return userRoleDao.getUserRoles(userId);
    }

    public boolean userHasRole(Long userId, String role) {
        return userRoleDao.userHasRole(userId, role);
    }

    public List<UserDto> getUsersByRole(String role) {
        return userRoleDao.getUsersByRole(role);
    }

    public boolean existsByUsername(String username) {
        return userDao.existsByName(username);
    }

    public boolean authenticate(String username, String password) {
        Optional<UserDto> user = userDao.findByNameAndPassword(username, password);
        return user.isPresent();
    }

    public Optional<UserDto> findByUsernameAndPassword(String username, String password) {
        Optional<UserDto> user = userDao.findByNameAndPassword(username, password);
        if (user.isPresent()) {
            UserDto userDto = user.get();
            Set<String> roles = userRoleDao.getUserRoles(userDto.getId());
            userDto.setRoles(roles);
        }
        return user;
    }
}