package com.example.habittracker.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_]{3,40}", message = "Логин: 3–40 латинских букв, цифр или _") String username,
    @NotBlank @Size(min = 8, max = 64, message = "Пароль: от 8 до 64 символов") String password,
    @Email @Size(max = 255) String email
) {}
