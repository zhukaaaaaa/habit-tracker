package com.zhuldyz.habittracker.dto;

import java.time.LocalDate;

public record RecordResponse(Long id, Long habitId, LocalDate date) {}
