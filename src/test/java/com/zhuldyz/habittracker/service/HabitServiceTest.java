package com.zhuldyz.habittracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.zhuldyz.habittracker.dto.HabitRequest;
import com.zhuldyz.habittracker.dto.HabitResponse;
import com.zhuldyz.habittracker.entity.Habit;
import com.zhuldyz.habittracker.entity.User;
import com.zhuldyz.habittracker.exception.HabitNotFoundException;
import com.zhuldyz.habittracker.mapper.HabitMapper;
import com.zhuldyz.habittracker.repository.HabitRepository;
import com.zhuldyz.habittracker.repository.UserRepository;
import com.zhuldyz.habittracker.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {
    @Mock HabitRepository repository;
    @Mock UserRepository users;
    @Mock CurrentUser currentUser;
    HabitService service;

    @BeforeEach void setUp() {
        service = new HabitService(repository, new HabitMapper(), currentUser, users);
    }
    private Habit ownedHabit() {
        Habit habit = new Habit(); habit.setId(1L); habit.setName("Read"); habit.setTarget(7);
        habit.setCreatedAt(LocalDateTime.of(2026, 4, 20, 12, 0));
        return habit;
    }
    private void owner() { when(currentUser.getId()).thenReturn(10L); }
    private void found() { owner(); when(repository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(ownedHabit())); }
    private void missing() { owner(); when(repository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.empty()); }

    @Test void createsHabitWithCurrentOwnerAndTimestamp() {
        owner(); User user = new User(); user.setId(10L);
        when(users.getReferenceById(10L)).thenReturn(user);
        when(repository.save(any())).thenAnswer(invocation -> {
            Habit habit = invocation.getArgument(0); habit.setId(1L);
            assertThat(habit.getUser()).isSameAs(user);
            return habit;
        });
        LocalDateTime before = LocalDateTime.now();
        var result = service.create(new HabitRequest("  Read  ", "Books", 7));
        assertThat(result.name()).isEqualTo("Read");
        assertThat(result.createdAt()).isBetween(before, LocalDateTime.now());
    }
    @Test void listsOnlyCurrentUsersHabits() {
        owner(); when(repository.findByUserIdOrderByIdAsc(10L)).thenReturn(List.of(ownedHabit()));
        assertThat(service.getAll()).extracting(HabitResponse::id).containsExactly(1L);
        verify(repository, never()).findAll();
    }
    @Test void findsOwnedHabit() {
        found(); assertThat(service.getById(1L).name()).isEqualTo("Read");
    }
    @Test void updatesOwnedHabit() {
        found(); when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(1L, new HabitRequest("Walk", "Park", 3));
        assertThat(result.name()).isEqualTo("Walk");
        assertThat(result.target()).isEqualTo(3);
        assertThat(result.id()).isEqualTo(1L);
    }
    @Test void deletesOwnedHabit() {
        found(); service.delete(1L);
        verify(repository).delete(argThat(habit -> habit.getId().equals(1L)));
    }
    @Test void missingHabitThrowsNotFound() {
        missing(); assertThatThrownBy(() -> service.getById(1L)).isInstanceOf(HabitNotFoundException.class);
    }
}
