package com.zhuldyz.habittracker.controller;

import java.time.LocalDate;
import java.util.List;
import com.zhuldyz.habittracker.dto.AuthResponse;
import com.zhuldyz.habittracker.dto.DailyStatsResponse;
import com.zhuldyz.habittracker.dto.RecordResponse;
import com.zhuldyz.habittracker.dto.RegisterResponse;
import com.zhuldyz.habittracker.dto.StatsResponse;
import com.zhuldyz.habittracker.dto.WeekStatsResponse;
import com.zhuldyz.habittracker.exception.ConflictException;
import com.zhuldyz.habittracker.exception.HabitNotFoundException;
import com.zhuldyz.habittracker.service.AuthService;
import com.zhuldyz.habittracker.service.RecordService;
import com.zhuldyz.habittracker.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, RecordController.class, StatsController.class})
@AutoConfigureMockMvc(addFilters = false)
class ApiControllersTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 20);
    private static final String CREDENTIALS = """
        {"username":"demo_user","password":"sample-password"}
        """;

    @Autowired MockMvc mvc;
    @MockitoBean AuthService auth;
    @MockitoBean RecordService records;
    @MockitoBean StatsService stats;

    @Test void registrationReturns201() throws Exception {
        when(auth.register(any())).thenReturn(new RegisterResponse("demo_user"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
            {"username":"demo_user","password":"sample-password","email":"demo@example.test"}
            """))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("demo_user"));
    }

    @Test void loginReturnsToken() throws Exception {
        when(auth.login(any())).thenReturn(new AuthResponse("token", "demo_user"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("token"));
    }

    @Test void invalidCredentialsReturn401() throws Exception {
        when(auth.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
            .andExpect(status().isUnauthorized());
    }

    @Test void completionReturns201() throws Exception {
        when(records.markCompleted(7L)).thenReturn(new RecordResponse(9L, 7L, TODAY));
        mvc.perform(post("/api/habits/7/records"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.habitId").value(7));
    }

    @Test void duplicateCompletionReturns409() throws Exception {
        when(records.markCompleted(7L)).thenThrow(new ConflictException("Already completed"));
        mvc.perform(post("/api/habits/7/records")).andExpect(status().isConflict());
    }

    @Test void recordHistoryReturnsEntries() throws Exception {
        when(records.getAll(7L)).thenReturn(List.of(new RecordResponse(9L, 7L, TODAY)));
        mvc.perform(get("/api/habits/7/records"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].date").value(TODAY.toString()));
    }

    @Test void habitStatisticsReturnStreakAndRate() throws Exception {
        when(stats.getHabitStats(7L)).thenReturn(new StatsResponse(2, 4, 50.0, 6));
        mvc.perform(get("/api/habits/7/stats"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.currentStreak").value(2))
            .andExpect(jsonPath("$.completionRate").value(50.0));
    }

    @Test void dailyStatisticsReturnCompletionStatus() throws Exception {
        when(stats.getDailyStats()).thenReturn(new DailyStatsResponse(TODAY,
            List.of(new DailyStatsResponse.HabitStatus(7L, "Read", true))));
        mvc.perform(get("/api/stats/daily"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.habits[0].completed").value(true));
    }

    @Test void weeklyStatisticsReturnCounts() throws Exception {
        when(stats.getWeekStats()).thenReturn(new WeekStatsResponse(
            List.of(new WeekStatsResponse.DayProgress(TODAY, 1, 2))));
        mvc.perform(get("/api/stats/week"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.week[0].completedCount").value(1))
            .andExpect(jsonPath("$.week[0].totalCount").value(2));
    }
}
