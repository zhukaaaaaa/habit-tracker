package com.example.habittracker.dto;

import java.time.LocalDateTime;

public record HabitResponse(Long id, String name, String description, int target, LocalDateTime createdAt) {}
