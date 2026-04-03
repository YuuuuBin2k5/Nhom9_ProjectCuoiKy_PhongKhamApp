package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Lớp Thực thể PrescriptionDetail (Chi tiết đơn thuốc) - Chứa thông tin về một loại thuốc cụ thể.
 * Liên kết với Prescription và tùy chọn liên kết với một bước điều trị trong phác đồ.
 */
@Entity
@Table(name = "prescription_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetail {
    // Thuộc tính private để bảo đảm tính đóng gói (Encapsulation).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    /**
     * Định danh bước/dịch vụ trong phác đồ để có thể kê đơn theo từng dịch vụ (step).
     * Tính năng kê đơn theo từng dịch vụ sẽ nhóm hiển thị theo field này ở phía mobile.
     */
    @Column(name = "treatment_plan_step_id")
    private Long treatmentPlanStepId;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    private String dosage;
    private String frequency;
    private String duration;
    private String unit;
    private Integer quantity;
}
