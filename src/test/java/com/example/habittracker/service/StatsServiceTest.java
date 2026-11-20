package com.example.habittracker.service;

import java.time.LocalDate;
import java.util.*;
import com.example.habittracker.dto.*;
import com.example.habittracker.entity.*;
import com.example.habittracker.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {
    @Mock HabitService habits;
    @Mock RecordRepository records;
    StatsService service;
    LocalDate today = LocalDate.of(2026, 4, 20);
    Habit habit;

    @BeforeEach void setUp() {
        service = new StatsService(habits, records);
        habit = new Habit();
        habit.setId(1L);
        habit.setName("Читать");
        habit.setCreatedAt(today.minusDays(6).atStartOfDay());
    }
    private void withDays(int... offsets) {
        when(habits.requireHabit(1L)).thenReturn(habit);
        var list = Arrays.stream(offsets).mapToObj(offset -> {
            var record = new HabitRecord();
            record.setHabit(habit);
            record.setDate(today.minusDays(offset));
            return record;
        }).toList();
        when(records.findByHabitIdOrderByDateAsc(1L)).thenReturn(list);
    }
    @Test void countsConsecutiveDays() {
        withDays(2, 1, 0);
        assertThat(service.getHabitStats(1L, today).currentStreak()).isEqualTo(3);
    }
    @Test void stopsAtMissingDay() {
        withDays(4, 3, 1, 0);
        assertThat(service.getHabitStats(1L, today).currentStreak()).isEqualTo(2);
    }
    @Test void findsBestHistoricalStreak() {
        withDays(6, 5, 4, 1, 0);
        assertThat(service.getHabitStats(1L, today).bestStreak()).isEqualTo(3);
    }
    @Test void calculatesRateIncludingCreationDay() {
        withDays(6, 4, 2, 1, 0);
        assertThat(service.getHabitStats(1L, today).completionRate()).isEqualTo(71.4);
    }
    @Test void todayMissingMeansCurrentStreakZero() {
        withDays(2, 1);
        assertThat(service.getHabitStats(1L, today).currentStreak()).isZero();
    }
    @Test void newHabitHasZeroRateWithoutCompletions() {
        habit.setCreatedAt(today.atStartOfDay());
        withDays();
        assertThat(service.getHabitStats(1L, today)).isEqualTo(new StatsResponse(0, 0, 0.0, 0));
    }
    @Test void dailySummaryUsesOnlyOwnedHabitIds() {
        var response = new HabitResponse(1L, "Читать", null, 7, habit.getCreatedAt());
        when(habits.getAll()).thenReturn(List.of(response));
        var record = new HabitRecord(); record.setHabit(habit); record.setDate(today);
        when(records.findInPeriod(List.of(1L), today, today)).thenReturn(List.of(record));
        assertThat(service.getDailyStats(today).habits().getFirst().completed()).isTrue();
    }
    @Test void weekDoesNotCountHabitBeforeCreation() {
        habit.setCreatedAt(today.atStartOfDay());
        when(habits.getAll()).thenReturn(List.of(new HabitResponse(1L, "Читать", null, 7, habit.getCreatedAt())));
        when(records.findInPeriod(List.of(1L), today.minusDays(6), today)).thenReturn(List.of());
        var week = service.getWeekStats(today).week();
        assertThat(week).hasSize(7);
        assertThat(week.getFirst().totalCount()).isZero();
        assertThat(week.getLast().totalCount()).isEqualTo(1);
    }
}
