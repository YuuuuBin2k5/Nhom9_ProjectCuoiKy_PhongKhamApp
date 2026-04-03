package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.UiTemplateType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Lớp Thực thể Service (Dịch vụ) - Danh mục các dịch vụ nha khoa (ví dụ: Trám răng, Nhổ răng).
 * Chứa thông tin về tên, mô tả và giá cơ bản của dịch vụ.
 */
@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    /**
     * Định danh duy nhất của dịch vụ.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Danh mục mà dịch vụ thuộc về.
     */
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

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.List<ServiceImage> images = new java.util.ArrayList<>();
}
