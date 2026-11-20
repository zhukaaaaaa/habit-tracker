package com.example.habittracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(@NotBlank @Size(max = 40) String username,
                          @NotBlank @Size(max = 64) String password) {}
