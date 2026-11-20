package com.zhuldyz.habittracker.controller;

import java.net.URI;
import java.util.List;
import com.zhuldyz.habittracker.dto.HabitRequest;
import com.zhuldyz.habittracker.dto.HabitResponse;
import com.zhuldyz.habittracker.service.HabitService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.zhuldyz.habittracker.exception.GlobalExceptionHandler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "A valid token is required", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {
    private final HabitService habitService;

    @PostMapping
    @Operation(summary = "Create a habit")
    @ApiResponse(responseCode = "201", description = "Habit created")
    public ResponseEntity<HabitResponse> create(@Valid @RequestBody HabitRequest request) {
        HabitResponse response = habitService.create(request);
        return ResponseEntity.created(URI.create("/api/habits/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "List habits")
    @ApiResponse(responseCode = "200", description = "Habit list")
    public List<HabitResponse> getAll() { return habitService.getAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Get a habit by ID")
    @ApiResponse(responseCode = "200", description = "Habit found")
    @ApiResponse(responseCode = "404", description = "Habit not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public HabitResponse getById(@PathVariable Long id) { return habitService.getById(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Update a habit")
    @ApiResponse(responseCode = "200", description = "Habit updated")
    @ApiResponse(responseCode = "404", description = "Habit not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public HabitResponse update(@PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        return habitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a habit")
    @ApiResponse(responseCode = "204", description = "Habit deleted")
    @ApiResponse(responseCode = "404", description = "Habit not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        habitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
