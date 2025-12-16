package ru.ssau.tk.avokado.lab2.dto;

public class UpdateUserRequest {
    private String name;
    private Integer accessLvl;
    private String password;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String name, Integer accessLvl, String password) {
        this.name = name;
        this.accessLvl = accessLvl;
        this.password = password;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAccessLvl() { return accessLvl; }
    public void setAccessLvl(Integer accessLvl) { this.accessLvl = accessLvl; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
