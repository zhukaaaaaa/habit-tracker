package com.zhuldyz.habittracker.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyStatsResponse(LocalDate date, List<HabitStatus> habits) {
    public record HabitStatus(Long id, String name, boolean completed) {}
}
