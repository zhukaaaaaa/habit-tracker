package com.example.habittracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record HabitRequest(
    @NotBlank(message = "Название не должно быть пустым") @Size(max = 255) String name,
    @Size(max = 5000) String description,
    @Positive(message = "Цель должна быть больше нуля") int target
) {}
