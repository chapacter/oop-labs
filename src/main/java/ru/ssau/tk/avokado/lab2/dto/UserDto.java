package ru.ssau.tk.avokado.lab2.dto;

import ru.ssau.tk.avokado.lab2.auth.Role;

import java.util.Set;

public class UserDto {
    private Long id;
    private String name;
    private Integer accessLvl;
    private Set<Role> roles;

    public UserDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAccessLvl() {
        return accessLvl;
    }

    public void setAccessLvl(Integer accessLvl) {
        this.accessLvl = accessLvl;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
