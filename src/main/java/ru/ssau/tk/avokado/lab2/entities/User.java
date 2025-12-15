package ru.ssau.tk.avokado.lab2.entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import ru.ssau.tk.avokado.lab2.auth.Role;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "access_lvl")
    private Integer accessLvl;

    @Column(name = "password_hash")
    private String passwordHash;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunctionEntity> functions;

    // NEW: роли пользователя
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    public User() {}

    // getters/setters (добавить для roles)
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAccessLvl() { return accessLvl; }
    public void setAccessLvl(Integer accessLvl) { this.accessLvl = accessLvl; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public List<FunctionEntity> getFunctions() { return functions; }
    public void setFunctions(List<FunctionEntity> functions) { this.functions = functions; }
}
