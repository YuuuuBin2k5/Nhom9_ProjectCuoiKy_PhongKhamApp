package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.TreatmentPlanStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "treatment_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(name = "template_id")
    private Long templateId; // Optional: reference to a template if any

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TreatmentPlanStatus status = TreatmentPlanStatus.IN_PROGRESS;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Builder.Default
    @Column(name = "is_draft")
    private boolean isDraft = true;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TreatmentPlanStep> steps;

}
