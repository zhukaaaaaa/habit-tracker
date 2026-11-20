package com.zhuldyz.habittracker.dto;

public record StatsResponse(int currentStreak, int bestStreak, double completionRate, int totalCompletions) {}
