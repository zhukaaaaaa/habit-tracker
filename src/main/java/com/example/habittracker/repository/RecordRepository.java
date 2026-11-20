package com.example.habittracker.repository;

import java.time.LocalDate;
import java.util.List;
import com.example.habittracker.entity.HabitRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<HabitRecord, Long> {
    List<HabitRecord> findByHabitIdOrderByDateAsc(Long habitId);
    boolean existsByHabitIdAndDate(Long habitId, LocalDate date);
}
