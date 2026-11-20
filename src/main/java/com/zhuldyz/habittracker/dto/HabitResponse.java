package com.zhuldyz.habittracker.dto;

import java.time.LocalDateTime;

public record HabitResponse(Long id, String name, String description, int target, LocalDateTime createdAt) {}
