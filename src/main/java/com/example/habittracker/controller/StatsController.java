package com.example.habittracker.controller;

import com.example.habittracker.dto.*;
import com.example.habittracker.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/api/habits/{id}/stats")
    @Operation(summary = "Серии и процент выполнения привычки")
    @ApiResponse(responseCode = "200", description = "Статистика привычки")
    public StatsResponse getHabitStats(@PathVariable Long id) { return statsService.getHabitStats(id); }

    @GetMapping("/api/stats/daily")
    @Operation(summary = "Сводка за сегодня")
    @ApiResponse(responseCode = "200", description = "Сегодняшние привычки и отметки")
    public DailyStatsResponse getDailyStats() { return statsService.getDailyStats(); }

    @GetMapping("/api/stats/week")
    @Operation(summary = "Прогресс за последние семь дней")
    @ApiResponse(responseCode = "200", description = "Сводка по дням")
    public WeekStatsResponse getWeekStats() { return statsService.getWeekStats(); }
}
