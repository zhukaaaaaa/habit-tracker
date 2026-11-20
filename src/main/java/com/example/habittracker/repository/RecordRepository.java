package com.example.habittracker.repository;

import java.time.LocalDate;
import java.util.List;
import com.example.habittracker.entity.HabitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecordRepository extends JpaRepository<HabitRecord, Long> {
    List<HabitRecord> findByHabitIdOrderByDateAsc(Long habitId);
    boolean existsByHabitIdAndDate(Long habitId, LocalDate date);

    @Query("select r from HabitRecord r where r.habit.id in :habitIds and r.date between :fromDate and :toDate")
    List<HabitRecord> findInPeriod(List<Long> habitIds, LocalDate fromDate, LocalDate toDate);
}
