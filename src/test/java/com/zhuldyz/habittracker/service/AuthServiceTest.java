package com.zhuldyz.habittracker.service;

import com.zhuldyz.habittracker.dto.AuthRequest;
import com.zhuldyz.habittracker.dto.AuthResponse;
import com.zhuldyz.habittracker.dto.RegisterRequest;
import com.zhuldyz.habittracker.repository.UserRepository;
import com.zhuldyz.habittracker.security.TrackerUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager manager;
    @Mock JwtService jwt;
    AuthService service;
    @BeforeEach void setUp() { service = new AuthService(users, encoder, manager, jwt); }

    @Test void registrationPassesOnlyEncodedPasswordToRepository() {
        when(users.existsByUsername("alice")).thenReturn(false);
        when(encoder.encode("password-21")).thenReturn("bcrypt-hash");
        var response = service.register(new RegisterRequest("alice", "password-21", "alice@example.test"));
        assertThat(response.username()).isEqualTo("alice");
        verify(users).saveAndFlush(argThat(user -> user.getPassword().equals("bcrypt-hash")));
    }
    @Test void loginReturnsTokenForAuthenticatedPrincipal() {
        var principal = new TrackerUserDetails(1L, "alice", "bcrypt-hash");
        when(manager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(jwt.generateToken(principal)).thenReturn("signed-token");
        assertThat(service.login(new AuthRequest("alice", "password-21")))
            .isEqualTo(new AuthResponse("signed-token", "alice"));
    }
    @Test void invalidPasswordNeverIssuesToken() {
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        assertThatThrownBy(() -> service.login(new AuthRequest("alice", "wrong"))).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwt);
    }
}
