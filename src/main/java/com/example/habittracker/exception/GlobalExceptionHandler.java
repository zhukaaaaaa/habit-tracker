package com.example.habittracker.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public record ApiError(int status, String message, List<String> details) {}

    @ExceptionHandler(HabitNotFoundException.class)
    public ResponseEntity<ApiError> notFound(HabitNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        var details = ex.getBindingResult().getFieldErrors().stream()
            .map(field -> field.getField() + ": " + field.getDefaultMessage()).toList();
        return error(HttpStatus.BAD_REQUEST, "Проверь введённые данные", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> badRequest(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "Неверный формат запроса", List.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), message, details));
    }
}
