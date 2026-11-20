package com.zhuldyz.habittracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record HabitRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most {max} characters long") String name,
    @Size(max = 5000, message = "Description must be at most {max} characters long") String description,
    @Positive(message = "Target must be greater than zero") int target
) {}
