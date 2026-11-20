package com.zhuldyz.habittracker.service;

import com.zhuldyz.habittracker.dto.HabitRequest;
import com.zhuldyz.habittracker.exception.HabitNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.zhuldyz.habittracker.entity.User;
import com.zhuldyz.habittracker.repository.UserRepository;
import com.zhuldyz.habittracker.security.TrackerUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        var result = service.create(new HabitRequest("Read", "10 pages", 7));
        assertThat(result.id()).isPositive();
        assertThat(result.name()).isEqualTo("Read");
    }
    @Test void findsExistingHabit() {
        var created = service.create(new HabitRequest("Water", null, 3));
        assertThat(service.getById(created.id())).isEqualTo(created);
    }
    @Test void rejectsMissingHabit() {
        assertThatThrownBy(() -> service.getById(Long.MAX_VALUE)).isInstanceOf(HabitNotFoundException.class);
    }
}
