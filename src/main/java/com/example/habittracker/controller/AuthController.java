package com.example.habittracker.controller;

import com.example.habittracker.dto.*;
import com.example.habittracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Зарегистрироваться")
    @ApiResponse(responseCode = "201", description = "Аккаунт создан")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) { return service.register(request); }

    @PostMapping("/login")
    @Operation(summary = "Войти и получить JWT")
    @ApiResponse(responseCode = "200", description = "JWT-токен и логин")
    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) { return service.login(request); }
}
