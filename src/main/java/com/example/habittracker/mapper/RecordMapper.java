package com.example.habittracker.mapper;

import com.example.habittracker.dto.RecordResponse;
import com.example.habittracker.entity.HabitRecord;
import org.springframework.stereotype.Component;

@Component
public class RecordMapper {
    public RecordResponse toResponse(HabitRecord record) {
        return new RecordResponse(record.getId(), record.getHabit().getId(), record.getDate());
    }
}
