package com.zhuldyz.habittracker.mapper;

import com.zhuldyz.habittracker.dto.RecordResponse;
import com.zhuldyz.habittracker.entity.Record;
import org.springframework.stereotype.Component;

@Component
public class RecordMapper {
    public RecordResponse toResponse(Record record) {
        return new RecordResponse(record.getId(), record.getHabit().getId(), record.getDate());
    }
}
