package com.example.habittracker.controller;

import java.net.URI;
import java.util.List;
import com.example.habittracker.dto.RecordResponse;
import com.example.habittracker.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/habits/{id}/records")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    @PostMapping
    @Operation(summary = "Отметить выполнение за сегодня")
    @ApiResponse(responseCode = "201", description = "Выполнение сохранено")
    @ApiResponse(responseCode = "409", description = "Уже отмечено сегодня")
    public ResponseEntity<RecordResponse> markCompleted(@PathVariable Long id) {
        RecordResponse record = recordService.markCompleted(id);
        return ResponseEntity.created(URI.create("/api/habits/" + id + "/records")).body(record);
    }

    @GetMapping
    @Operation(summary = "История выполнений привычки")
    @ApiResponse(responseCode = "200", description = "Список выполнений")
    public List<RecordResponse> getAll(@PathVariable Long id) { return recordService.getAll(id); }
}
