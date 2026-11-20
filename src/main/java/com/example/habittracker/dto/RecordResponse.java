package com.example.habittracker.dto;

import java.time.LocalDate;

public record RecordResponse(Long id, Long habitId, LocalDate date) {}
