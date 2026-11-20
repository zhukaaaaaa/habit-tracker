package com.zhuldyz.habittracker.service;

import com.zhuldyz.habittracker.dto.AuthRequest;
import com.zhuldyz.habittracker.dto.AuthResponse;
import com.zhuldyz.habittracker.dto.RegisterRequest;
import com.zhuldyz.habittracker.dto.RegisterResponse;
import com.zhuldyz.habittracker.entity.User;
import com.zhuldyz.habittracker.exception.ConflictException;
import com.zhuldyz.habittracker.exception.InvalidRequestException;
import com.zhuldyz.habittracker.repository.UserRepository;
import com.zhuldyz.habittracker.security.TrackerUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        User user = new User();
        user.setUsername(request.username());
        try {
            user.setPassword(encoder.encode(request.password()));
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Password is too long");
        }
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
