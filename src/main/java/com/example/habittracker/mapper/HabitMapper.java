package com.example.habittracker.mapper;

import com.example.habittracker.dto.HabitResponse;
import com.example.habittracker.entity.Habit;
import org.springframework.stereotype.Component;

@Component
public class HabitMapper {
    public HabitResponse toResponse(Habit habit) {
        return new HabitResponse(habit.getId(), habit.getName(), habit.getDescription(), habit.getTarget(), habit.getCreatedAt());
    }
}
