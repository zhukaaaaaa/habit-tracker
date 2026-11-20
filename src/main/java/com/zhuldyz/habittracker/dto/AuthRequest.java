package com.zhuldyz.habittracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
    @NotBlank(message = "Username is required")
    @Size(max = 40, message = "Username must be at most {max} characters long") String username,
    @NotBlank(message = "Password is required")
    @Size(max = 64, message = "Password must be at most {max} characters long") String password
) {}
