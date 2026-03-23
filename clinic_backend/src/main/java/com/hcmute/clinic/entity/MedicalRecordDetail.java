package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

@Entity
@Table(name = "medical_record_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "tooth_number")
    private String toothNumber; // FDI notation

    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "treatment_note", columnDefinition = "TEXT")
    private String treatmentNote;
}
