package com.zhuldyz.habittracker.service;

import java.time.LocalDate;
import java.util.List;
import com.zhuldyz.habittracker.dto.RecordResponse;
import com.zhuldyz.habittracker.entity.Record;
import com.zhuldyz.habittracker.exception.ConflictException;
import com.zhuldyz.habittracker.mapper.RecordMapper;
import com.zhuldyz.habittracker.repository.RecordRepository;
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
            throw new ConflictException("This habit has already been marked complete today");
        }
        Record record = new Record();
        record.setHabit(habit);
        record.setDate(today);
        return mapper.toResponse(repository.saveAndFlush(record));
    }

    public List<RecordResponse> getAll(Long habitId) {
        habitService.requireHabit(habitId);
        return repository.findByHabitIdOrderByDateAsc(habitId).stream().map(mapper::toResponse).toList();
    }
}
