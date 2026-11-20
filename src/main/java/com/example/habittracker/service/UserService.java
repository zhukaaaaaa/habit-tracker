package com.example.habittracker.service;

import com.example.habittracker.repository.UserRepository;
import com.example.habittracker.security.TrackerUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public TrackerUserDetails loadUserByUsername(String username) {
        var user = repository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new TrackerUserDetails(user.getId(), user.getUsername(), user.getPassword());
    }
}
