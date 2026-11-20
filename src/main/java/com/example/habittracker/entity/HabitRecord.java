package com.example.habittracker.entity;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitRecord {
    private Long id;
    private Long habitId;
    private LocalDate date;
}
