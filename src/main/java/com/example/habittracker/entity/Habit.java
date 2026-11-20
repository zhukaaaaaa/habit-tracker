package com.example.habittracker.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Habit {
    private Long id;
    private String name;
    private String description;
    private int target;
    private LocalDateTime createdAt;
}
