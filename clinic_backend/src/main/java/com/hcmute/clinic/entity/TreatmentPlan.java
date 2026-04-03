package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.TreatmentPlanStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp Thực thể TreatmentPlan (Phác đồ điều trị) - Khung sườn cho các flow SE_13, SE_14, SE_15.
 * Quản lý tập hợp các bước điều trị dài hạn cho bệnh nhân, đảm bảo tính liên tục của quy trình nha khoa.
 */
@Entity
@Table(name = "treatment_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlan {
    // Encapsulation: Chế độ truy cập private cho tất cả các trường.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ N-1: Phác đồ được lập cho một bệnh nhân cụ thể.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

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

    // Quan hệ 1-N: Một phác đồ chứa nhiều bước điều trị cụ thể trong flow SE_14.
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.Set<TreatmentPlanStep> steps;

}
