package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp Thực thể MedicalRecord (Hồ sơ bệnh án) - Lưu trữ kết quả của flow SE_14 (Ghi nhận kết quả khám).
 * Là nơi tổng hợp chẩn đoán, triệu chứng và các tư vấn chuyên môn từ bác sĩ.
 */
@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {
    // Encapsulation: Đảm bảo các thuộc tính ở trạng thái private.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ 1-1: Mỗi hồ sơ bệnh án thuộc về một lịch hẹn cụ thể.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<MedicalRecordDetail> details;

    @OneToOne(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private Prescription prescription;

    @OneToOne(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TreatmentPlan treatmentPlan;
}
