package com.example.habittracker.service;

import com.example.habittracker.dto.HabitRequest;
import com.example.habittracker.exception.HabitNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.example.habittracker.entity.User;
import com.example.habittracker.repository.UserRepository;
import com.example.habittracker.security.TrackerUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class HabitPersistenceTest {
    @Autowired HabitService service;
    @Autowired UserRepository users;

    @BeforeEach void authenticate() {
        User user = new User(); user.setUsername("persistence-test"); user.setPassword("unused");
        user = users.saveAndFlush(user);
        var principal = new TrackerUserDetails(user.getId(), user.getUsername(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void createsHabit() {
        var result = service.create(new HabitRequest("Читать", "10 страниц", 7));
        assertThat(result.id()).isPositive();
        assertThat(result.name()).isEqualTo("Читать");
    }
    @Test void findsExistingHabit() {
        var created = service.create(new HabitRequest("Вода", null, 3));
        assertThat(service.getById(created.id())).isEqualTo(created);
    }
    @Test void rejectsMissingHabit() {
        assertThatThrownBy(() -> service.getById(Long.MAX_VALUE)).isInstanceOf(HabitNotFoundException.class);
    }
}
