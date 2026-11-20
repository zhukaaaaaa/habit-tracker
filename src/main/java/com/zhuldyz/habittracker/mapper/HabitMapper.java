package com.zhuldyz.habittracker.mapper;

import com.zhuldyz.habittracker.dto.HabitResponse;
import com.zhuldyz.habittracker.dto.HabitRequest;
import com.zhuldyz.habittracker.entity.Habit;
import org.springframework.stereotype.Component;

@Component
public class HabitMapper {
    public void applyRequest(Habit habit, HabitRequest request) {
        habit.setName(request.name().strip());
        habit.setDescription(request.description());
        habit.setTarget(request.target());
    }

    public HabitResponse toResponse(Habit habit) {
        return new HabitResponse(habit.getId(), habit.getName(), habit.getDescription(), habit.getTarget(), habit.getCreatedAt());
    }
}
