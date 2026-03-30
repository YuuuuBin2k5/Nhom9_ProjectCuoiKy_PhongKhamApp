package com.hcmute.clinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmute.clinic.enums.StepStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "treatment_plan_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlanStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TreatmentPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "appointment_id")
    private Long appointmentId; // Use ID to avoid circularity or separate scheduling

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_room_id")
    private ClinicRoom clinicRoom;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "actual_price")
    private BigDecimal actualPrice;

    @Column(name = "doctor_conclusion", columnDefinition = "TEXT")
    private String doctorConclusion;

    @Column(name = "tooth_number")
    private String toothNumber; // FDI notation, e.g. "38" for SURGERY/ODONTOGRAM

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "is_general_service")
    private boolean isGeneralService = false;
    // true = dịch vụ tổng quát (toothNumber = null)
    // false = dịch vụ cụ thể cho một răng (toothNumber != null)

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StepImage> images;

    @OneToOne(mappedBy = "step", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Prescription prescription;

}
