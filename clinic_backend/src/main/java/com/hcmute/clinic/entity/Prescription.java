package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp Thực thể Prescription (Đơn thuốc) - Thành phần tùy chọn trong flow SE_14 (Ghi nhận kết quả).
 * Chứa danh sách các loại thuốc và hướng dẫn sử dụng cho bệnh nhân sau khi thăm khám.
 */
@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {
    // Encapsulation: Chế độ truy cập private cho tất cả các trường.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ 1-1: Mỗi đơn thuốc liên kết với một hồ sơ bệnh án MedicalRecord.
    @OneToOne
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    // Quan hệ N-1: Đơn thuốc có thể được kê riêng lẻ cho từng bước trong phác đồ điều trị.
    @ManyToOne
    @JoinColumn(name = "step_id")
    private TreatmentPlanStep step;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL)
    private List<PrescriptionDetail> details;
}
