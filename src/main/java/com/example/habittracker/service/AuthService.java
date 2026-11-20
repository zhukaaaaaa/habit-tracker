package com.example.habittracker.service;

import java.nio.charset.StandardCharsets;
import com.example.habittracker.dto.*;
import com.example.habittracker.entity.User;
import com.example.habittracker.exception.*;
import com.example.habittracker.repository.UserRepository;
import com.example.habittracker.security.TrackerUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new InvalidRequestException("Пароль слишком длинный в UTF-8: максимум 72 байта");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Этот логин уже занят");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(encoder.encode(request.password()));
        user.setEmail(request.email());
        userRepository.saveAndFlush(user);
        return new RegisterResponse(user.getUsername());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = (TrackerUserDetails) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(user), user.username());
    }
}
