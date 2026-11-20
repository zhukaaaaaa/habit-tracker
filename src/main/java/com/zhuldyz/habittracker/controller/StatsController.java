package com.zhuldyz.habittracker.controller;

import com.zhuldyz.habittracker.dto.DailyStatsResponse;
import com.zhuldyz.habittracker.dto.StatsResponse;
import com.zhuldyz.habittracker.dto.WeekStatsResponse;
import com.zhuldyz.habittracker.service.StatsService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.zhuldyz.habittracker.exception.GlobalExceptionHandler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "A valid token is required", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/api/habits/{id}/stats")
    @Operation(summary = "Get habit streaks and completion rate")
    @ApiResponse(responseCode = "200", description = "Habit statistics")
    @ApiResponse(responseCode = "404", description = "Habit not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public StatsResponse getHabitStats(@PathVariable Long id) { return statsService.getHabitStats(id); }

    @GetMapping("/api/stats/daily")
    @Operation(summary = "Get today's summary")
    @ApiResponse(responseCode = "200", description = "Today's habits and completions")
    public DailyStatsResponse getDailyStats() { return statsService.getDailyStats(); }

    @GetMapping("/api/stats/week")
    @Operation(summary = "Get progress for the last seven days")
    @ApiResponse(responseCode = "200", description = "Daily progress summary")
    public WeekStatsResponse getWeekStats() { return statsService.getWeekStats(); }
}
