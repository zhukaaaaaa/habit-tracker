package com.example.habittracker.security;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public Long getId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof TrackerUserDetails user)) {
            throw new InsufficientAuthenticationException("Сначала войди в аккаунт");
        }
        return user.id();
    }
}
