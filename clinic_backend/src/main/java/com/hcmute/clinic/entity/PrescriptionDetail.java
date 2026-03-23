package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    private String dosage;
    private String frequency;
    private String duration;
    private String unit;
}
