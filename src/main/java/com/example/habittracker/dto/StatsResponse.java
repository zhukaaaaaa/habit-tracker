package com.example.habittracker.dto;

public record StatsResponse(int currentStreak, int bestStreak, double completionRate, int totalCompletions) {}
