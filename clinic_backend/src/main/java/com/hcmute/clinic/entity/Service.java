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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "ui_template_type")
    private UiTemplateType uiTemplateType = UiTemplateType.GENERAL;

    @Column(name = "default_monitoring_days")
    private Integer defaultMonitoringDays; // Số ngày chờ mặc định sau buổi dịch vụ này
                                           // null = không có giai đoạn theo dõi
                                           // VD: nhổ răng khôn = 7

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.List<ServiceImage> images = new java.util.ArrayList<>();
}
