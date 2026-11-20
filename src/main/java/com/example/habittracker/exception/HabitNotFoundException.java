package com.example.habittracker.exception;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(Long id) {
        super("Привычка с ID " + id + " не найдена");
    }
}
