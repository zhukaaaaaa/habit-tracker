package com.example.habittracker.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "habits")
@Getter
@Setter
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private int target;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HabitRecord> records = new ArrayList<>();
}
