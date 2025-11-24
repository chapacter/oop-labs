package ru.ssau.tk.avokado.lab2.dto;

import ru.ssau.tk.avokado.lab2.Role;

public class UserDto {
    private Long id;
    private String name;
    private Integer accessLvl;
    private String passwordHash;
    private Integer pointCount; // для статистики

    public UserDto(String name, Integer accessLvl, String passwordHash) {
        this.name = name;
        this.accessLvl = accessLvl;
        this.passwordHash = passwordHash;
    }

    public UserDto(Long id, String name, Integer accessLvl, String passwordHash) {
        this.id = id;
        this.name = name;
        this.accessLvl = accessLvl;
        this.passwordHash = passwordHash;
    }

    // Конструктор для статистики пользователя
    public UserDto(Long id, String name, Integer pointCount) {
        this.id = id;
        this.name = name;
        this.pointCount = pointCount;
    }

    public UserDto(String name, String passwordHash, Role role) {
        this.name = name;
        this.passwordHash = passwordHash;
        this.accessLvl = role == Role.ADMIN ? 1 : 0;
    }

    public UserDto(String name, String passwordHash) {
        this(name, 0, passwordHash);
    }

    public UserDto(Long id, String name, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.accessLvl = role == Role.ADMIN ? 1 : 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAccessLvl() { return accessLvl; }
    public void setAccessLvl(Integer accessLvl) { this.accessLvl = accessLvl; }

    public Integer getPointCount() { return pointCount; }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    // Методы для совместимости с тестами
    public String getUsername() {
        return name;
    }
    
    public Role getRole() {
        return accessLvl == 1 ? Role.ADMIN : Role.USER;
    }
    
    public void setUsername(String name) {
        this.name = name;
    }
    
    public void setRole(Role role) {
        this.accessLvl = role == Role.ADMIN ? 1 : 0;
    }
}
