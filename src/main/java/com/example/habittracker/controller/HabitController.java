package com.example.habittracker.controller;

import java.net.URI;
import java.util.List;
import com.example.habittracker.dto.*;
import com.example.habittracker.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {
    private final HabitService habitService;

    @PostMapping
    @Operation(summary = "Создать привычку")
    @ApiResponse(responseCode = "201", description = "Привычка создана")
    public ResponseEntity<HabitResponse> create(@Valid @RequestBody HabitRequest request) {
        HabitResponse response = habitService.create(request);
        return ResponseEntity.created(URI.create("/api/habits/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Показать привычки")
    @ApiResponse(responseCode = "200", description = "Список привычек")
    public List<HabitResponse> getAll() { return habitService.getAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Найти привычку по ID")
    @ApiResponse(responseCode = "200", description = "Привычка найдена")
    @ApiResponse(responseCode = "404", description = "Привычка не найдена")
    public HabitResponse getById(@PathVariable Long id) { return habitService.getById(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить привычку")
    @ApiResponse(responseCode = "200", description = "Привычка изменена")
    public HabitResponse update(@PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        return habitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить привычку")
    @ApiResponse(responseCode = "204", description = "Привычка удалена")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        habitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
