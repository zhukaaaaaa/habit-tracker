package com.example.habittracker.service;

import java.time.LocalDateTime;
import java.util.*;
import com.example.habittracker.dto.*;
import com.example.habittracker.entity.Habit;
import com.example.habittracker.exception.HabitNotFoundException;
import com.example.habittracker.mapper.HabitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitService {
    private final HabitMapper mapper;
    private final Map<Long, Habit> habits = new HashMap<>();
    private long nextId = 1;

    public synchronized HabitResponse create(HabitRequest request) {
        Habit habit = new Habit();
        habit.setId(nextId++);
        habit.setCreatedAt(LocalDateTime.now());
        apply(habit, request);
        habits.put(habit.getId(), habit);
        log.info("Created habit id={}", habit.getId());
        return mapper.toResponse(habit);
    }

    public synchronized List<HabitResponse> getAll() {
        return habits.values().stream().sorted(Comparator.comparing(Habit::getId)).map(mapper::toResponse).toList();
    }

    public synchronized HabitResponse getById(Long id) { return mapper.toResponse(requireHabit(id)); }

    public synchronized HabitResponse update(Long id, HabitRequest request) {
        Habit habit = requireHabit(id);
        apply(habit, request);
        return mapper.toResponse(habit);
    }

    public synchronized void delete(Long id) {
        requireHabit(id);
        habits.remove(id);
        log.info("Deleted habit id={}", id);
    }

    private Habit requireHabit(Long id) {
        Habit habit = habits.get(id);
        if (habit == null) throw new HabitNotFoundException(id);
        return habit;
    }

    private void apply(Habit habit, HabitRequest request) {
        habit.setName(request.name().strip());
        habit.setDescription(request.description());
        habit.setTarget(request.target());
    }
}
