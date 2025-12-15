package ru.ssau.tk.avokado.lab2.dto;

public class CreateUserRequest {
    private String name;
    private String password;
    private Integer accessLvl;

    public CreateUserRequest() {}

    public CreateUserRequest(String name, String password, Integer accessLvl) {
        this.name = name;
        this.password = password;
        this.accessLvl = accessLvl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getAccessLvl() { return accessLvl; }
    public void setAccessLvl(Integer accessLvl) { this.accessLvl = accessLvl; }
}
