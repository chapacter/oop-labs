package ru.ssau.tk.avokado.lab2.dto;

public record CreatePointRequest(Long functionId, Integer indexInFunction, double x, double y) {}
