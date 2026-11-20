package com.example.habittracker.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "records", uniqueConstraints = @UniqueConstraint(name = "uk_records_habit_date", columnNames = {"habit_id", "date"}))
@Getter
@Setter
public class HabitRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;
    @Column(nullable = false)
    private LocalDate date;
}
