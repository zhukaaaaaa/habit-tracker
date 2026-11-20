package com.example.habittracker.repository;

import java.util.List;
import java.util.Optional;
import com.example.habittracker.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserIdOrderByIdAsc(Long userId);
    Optional<Habit> findByIdAndUserId(Long id, Long userId);
}
