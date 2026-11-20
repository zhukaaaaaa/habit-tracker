package com.example.habittracker.service;

import java.time.LocalDate;
import java.util.*;
import com.example.habittracker.dto.RecordResponse;
import com.example.habittracker.entity.HabitRecord;
import com.example.habittracker.exception.ConflictException;
import com.example.habittracker.mapper.RecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final HabitService habitService;
    private final RecordMapper mapper;
    private final Map<Long, HabitRecord> records = new HashMap<>();
    private long nextId = 1;

    public synchronized RecordResponse markCompleted(Long habitId) {
        habitService.getById(habitId);
        LocalDate today = LocalDate.now();
        if (records.values().stream().anyMatch(r -> r.getHabitId().equals(habitId) && r.getDate().equals(today))) {
            throw new ConflictException("За сегодня выполнение уже отмечено");
        }
        HabitRecord record = new HabitRecord();
        record.setId(nextId++);
        record.setHabitId(habitId);
        record.setDate(today);
        records.put(record.getId(), record);
        return mapper.toResponse(record);
    }

    public synchronized List<RecordResponse> getAll(Long habitId) {
        habitService.getById(habitId);
        return records.values().stream().filter(r -> r.getHabitId().equals(habitId))
            .sorted(Comparator.comparing(HabitRecord::getDate)).map(mapper::toResponse).toList();
    }
}
