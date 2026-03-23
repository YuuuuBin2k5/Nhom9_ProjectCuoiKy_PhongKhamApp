package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.UiTemplateType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "ui_template_type")
    private UiTemplateType uiTemplateType = UiTemplateType.GENERAL;
}
