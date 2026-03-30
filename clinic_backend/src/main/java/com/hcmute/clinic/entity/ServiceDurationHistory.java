package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_duration_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDurationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "scheduled_duration_minutes", nullable = false)
    private Integer scheduledDuration;

    @Column(name = "actual_duration_minutes", nullable = false)
    private Integer actualDuration;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Builder.Default
    @Column(name = "had_complications")
    private Boolean hadComplications = false;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
