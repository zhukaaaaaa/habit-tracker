package com.example.habittracker.dto;

import java.time.LocalDate;
import java.util.List;

public record WeekStatsResponse(List<DayProgress> week) {
    public record DayProgress(LocalDate date, long completedCount, long totalCount) {}
}
