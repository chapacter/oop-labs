package ru.ssau.tk.avokado.lab2.dto;

public record FunctionDto(Long id,
                          String name,
                          Integer format,
                          String funcResult,
                          SimpleUserDto user,
                          Long pointsCount) {}
