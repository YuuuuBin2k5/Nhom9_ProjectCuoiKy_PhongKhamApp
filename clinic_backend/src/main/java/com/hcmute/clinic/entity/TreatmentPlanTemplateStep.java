package com.hcmute.clinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "treatment_plan_template_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlanTemplateStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnore
    private TreatmentPlanTemplate template;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "clinic_room_id")
    private ClinicRoom clinicRoom;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;
}
