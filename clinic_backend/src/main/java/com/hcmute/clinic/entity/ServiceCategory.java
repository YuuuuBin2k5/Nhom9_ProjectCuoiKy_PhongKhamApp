package com.hcmute.clinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Lớp Thực thể ServiceCategory (Danh mục Dịch vụ) - Phân loại các dịch vụ nha khoa.
 * Ví dụ: Nha khoa tổng quát, Thẩm mỹ, Chỉnh nha.
 */
@Entity
@Table(name = "service_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCategory {
    public ServiceCategory(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Service> services;
}
