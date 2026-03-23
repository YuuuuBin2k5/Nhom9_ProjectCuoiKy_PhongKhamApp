package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clinic_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer capacity;
}
