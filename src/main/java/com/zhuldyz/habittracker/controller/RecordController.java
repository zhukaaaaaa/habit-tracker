package com.zhuldyz.habittracker.controller;

import java.net.URI;
import java.util.List;
import com.zhuldyz.habittracker.dto.RecordResponse;
import com.zhuldyz.habittracker.service.RecordService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.zhuldyz.habittracker.exception.GlobalExceptionHandler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "A valid token is required", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
@RestController
@RequestMapping("/api/habits/{id}/records")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    @PostMapping
    @Operation(summary = "Mark a habit complete for today")
    @ApiResponse(responseCode = "201", description = "Completion recorded")
    @ApiResponse(responseCode = "404", description = "Habit not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Habit already marked complete today", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<RecordResponse> markCompleted(@PathVariable Long id) {
        RecordResponse record = recordService.markCompleted(id);
        return ResponseEntity.created(URI.create("/api/habits/" + id + "/records")).body(record);
    }

    @GetMapping
    @Operation(summary = "Get completion history")
    @ApiResponse(responseCode = "200", description = "Completion records")
    @ApiResponse(responseCode = "404", description = "Habit not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<RecordResponse> getAll(@PathVariable Long id) { return recordService.getAll(id); }
}
