package com.zhuldyz.habittracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "[a-zA-Z0-9_]{3,40}",
        message = "Username must be 3 to 40 characters long and contain only English letters, digits, or underscores")
    String username,
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters long") String password,
    @Email(message = "Email address is invalid")
    @Size(max = 255, message = "Email must be at most {max} characters long") String email
) {}
