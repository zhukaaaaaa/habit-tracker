package com.example.habittracker.service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.habittracker.dto.*;
import com.example.habittracker.entity.Habit;
import com.example.habittracker.exception.HabitNotFoundException;
import com.example.habittracker.mapper.HabitMapper;
import com.example.habittracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.habittracker.repository.UserRepository;
import com.example.habittracker.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HabitService {
    private final HabitRepository repository;
    private final HabitMapper mapper;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    @Transactional
    public HabitResponse create(HabitRequest request) {
        Habit habit = new Habit();
        habit.setUser(userRepository.getReferenceById(currentUser.getId()));
        habit.setCreatedAt(LocalDateTime.now());
        apply(habit, request);
        habit = repository.save(habit);
        log.info("Created habit id={}", habit.getId());
        return mapper.toResponse(habit);
    }

    public List<HabitResponse> getAll() {
        return repository.findByUserIdOrderByIdAsc(currentUser.getId()).stream().map(mapper::toResponse).toList();
    }

    public HabitResponse getById(Long id) { return mapper.toResponse(requireHabit(id)); }

    @Transactional
    public HabitResponse update(Long id, HabitRequest request) {
        Habit habit = requireHabit(id);
        apply(habit, request);
        return mapper.toResponse(repository.save(habit));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(requireHabit(id));
        log.info("Deleted habit id={}", id);
    }

    public Habit requireHabit(Long id) {
        return repository.findByIdAndUserId(id, currentUser.getId()).orElseThrow(() -> new HabitNotFoundException(id));
    }

    private void apply(Habit habit, HabitRequest request) {
        habit.setName(request.name().strip());
        habit.setDescription(request.description());
        habit.setTarget(request.target());
    }
}
