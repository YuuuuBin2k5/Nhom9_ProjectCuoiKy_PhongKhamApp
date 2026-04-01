package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Patient patient;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "underlying_conditions", columnDefinition = "TEXT")
    private String underlyingConditions;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "last_updated")
    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}
