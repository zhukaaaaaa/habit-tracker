package com.zhuldyz.habittracker.service;

import java.time.LocalDateTime;
import java.util.List;
import com.zhuldyz.habittracker.dto.HabitRequest;
import com.zhuldyz.habittracker.dto.HabitResponse;
import com.zhuldyz.habittracker.entity.Habit;
import com.zhuldyz.habittracker.exception.HabitNotFoundException;
import com.zhuldyz.habittracker.mapper.HabitMapper;
import com.zhuldyz.habittracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zhuldyz.habittracker.repository.UserRepository;
import com.zhuldyz.habittracker.security.CurrentUser;
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
        mapper.applyRequest(habit, request);
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
        mapper.applyRequest(habit, request);
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

}
