package com.zhuldyz.habittracker.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.zhuldyz.habittracker.dto.DailyStatsResponse;
import com.zhuldyz.habittracker.dto.HabitResponse;
import com.zhuldyz.habittracker.dto.StatsResponse;
import com.zhuldyz.habittracker.dto.WeekStatsResponse;
import com.zhuldyz.habittracker.entity.Record;
import com.zhuldyz.habittracker.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {
    private static final int DAYS_IN_WEEK = 7;
    private final HabitService habitService;
    private final RecordRepository recordRepository;

    public StatsResponse getHabitStats(Long id) {
        return getHabitStats(id, LocalDate.now());
    }

    StatsResponse getHabitStats(Long id, LocalDate today) {
        var habit = habitService.requireHabit(id);
        LocalDate created = habit.getCreatedAt().toLocalDate();
        var dates = recordRepository.findByHabitIdOrderByDateAsc(id).stream()
            .map(Record::getDate).filter(date -> !date.isBefore(created) && !date.isAfter(today))
            .collect(Collectors.toCollection(TreeSet::new));

        int currentStreak = 0;
        for (LocalDate date = today; dates.contains(date); date = date.minusDays(1)) {
            currentStreak++;
        }

        int bestStreak = 0;
        int streak = 0;
        LocalDate previous = null;
        for (LocalDate date : dates) {
            streak = previous != null && previous.plusDays(1).equals(date) ? streak + 1 : 1;
            bestStreak = Math.max(bestStreak, streak);
            previous = date;
        }

        long days = Math.max(1, ChronoUnit.DAYS.between(created, today) + 1);
        double rate = Math.round(dates.size() * 1000.0 / days) / 10.0;
        return new StatsResponse(currentStreak, bestStreak, rate, dates.size());
    }

    public DailyStatsResponse getDailyStats() {
        LocalDate today = LocalDate.now();
        var habits = habitService.getAll();
        var records = recordsFor(habits, today, today);
        Set<Long> completed = records.stream().map(r -> r.getHabit().getId()).collect(Collectors.toSet());
        var statuses = habits.stream().map(habit -> new DailyStatsResponse.HabitStatus(
            habit.id(), habit.name(), completed.contains(habit.id()))).toList();
        return new DailyStatsResponse(today, statuses);
    }

    public WeekStatsResponse getWeekStats() {
        LocalDate today = LocalDate.now();
        LocalDate first = today.minusDays(DAYS_IN_WEEK - 1);
        var habits = habitService.getAll();
        var records = recordsFor(habits, first, today);
        var week = IntStream.range(0, DAYS_IN_WEEK).mapToObj(offset -> {
            LocalDate date = first.plusDays(offset);
            long total = habits.stream().filter(h -> !h.createdAt().toLocalDate().isAfter(date)).count();
            long completed = records.stream().filter(r -> r.getDate().equals(date))
                .map(r -> r.getHabit().getId()).distinct().count();
            return new WeekStatsResponse.DayProgress(date, completed, total);
        }).toList();
        return new WeekStatsResponse(week);
    }

    private List<Record> recordsFor(List<HabitResponse> habits, LocalDate first, LocalDate last) {
        if (habits.isEmpty()) return List.of();
        return recordRepository.findInPeriod(habits.stream().map(HabitResponse::id).toList(), first, last);
    }
}
