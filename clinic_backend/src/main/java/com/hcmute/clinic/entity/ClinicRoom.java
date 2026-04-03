package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Lớp Thực thể ClinicRoom (Phòng khám) - Tài nguyên vật lý nơi thực hiện các bước điều trị.
 * Mỗi phòng có thể được gán cho một hoặc nhiều bác sĩ/dịch vụ nhất định.
 */
@Entity
@Table(name = "clinic_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicRoom {
    // Encapsulation.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;
}
