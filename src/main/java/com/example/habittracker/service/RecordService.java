package com.example.habittracker.service;

import java.time.LocalDate;
import java.util.List;
import com.example.habittracker.dto.RecordResponse;
import com.example.habittracker.entity.HabitRecord;
import com.example.habittracker.exception.ConflictException;
import com.example.habittracker.mapper.RecordMapper;
import com.example.habittracker.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {
    private final HabitService habitService;
    private final RecordRepository repository;
    private final RecordMapper mapper;

    @Transactional
    public RecordResponse markCompleted(Long habitId) {
        var habit = habitService.requireHabit(habitId);
        LocalDate today = LocalDate.now();
        if (repository.existsByHabitIdAndDate(habitId, today)) {
            throw new ConflictException("За сегодня выполнение уже отмечено");
        }
        HabitRecord record = new HabitRecord();
        record.setHabit(habit);
        record.setDate(today);
        return mapper.toResponse(repository.saveAndFlush(record));
    }

    public List<RecordResponse> getAll(Long habitId) {
        habitService.requireHabit(habitId);
        return repository.findByHabitIdOrderByDateAsc(habitId).stream().map(mapper::toResponse).toList();
    }
}
