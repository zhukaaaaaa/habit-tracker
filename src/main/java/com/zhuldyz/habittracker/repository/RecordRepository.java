package com.zhuldyz.habittracker.repository;

import java.time.LocalDate;
import java.util.List;
import com.zhuldyz.habittracker.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecordRepository extends JpaRepository<Record, Long> {
    List<Record> findByHabitIdOrderByDateAsc(Long habitId);
    boolean existsByHabitIdAndDate(Long habitId, LocalDate date);

    @Query("select r from Record r where r.habit.id in :habitIds and r.date between :fromDate and :toDate")
    List<Record> findInPeriod(List<Long> habitIds, LocalDate fromDate, LocalDate toDate);
}
