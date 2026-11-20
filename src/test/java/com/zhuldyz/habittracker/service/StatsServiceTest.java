package com.zhuldyz.habittracker.service;

import java.time.LocalDate;
import java.util.Arrays;
import com.zhuldyz.habittracker.entity.Habit;
import com.zhuldyz.habittracker.entity.Record;
import com.zhuldyz.habittracker.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
        habit.setName("Read");
        habit.setCreatedAt(today.minusDays(6).atStartOfDay());
    }
    private void withDays(int... offsets) {
        when(habits.requireHabit(1L)).thenReturn(habit);
        var list = Arrays.stream(offsets).mapToObj(offset -> {
            var record = new Record();
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
}
