package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.enums.BookingType;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Lớp Thực thể Appointment (Lịch hẹn) - Quản lý thông tin đặt lịch giữa Bệnh nhân và Bác sĩ.
 * Lưu trữ thời gian, loại dịch vụ và trạng thái của cuộc hẹn.
 */
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    /** Dịch vụ được yêu cầu trong lịch hẹn */
    private Service service;

    @Column(name = "plan_step_id")
    private Long planStepId;

    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDatetime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type")
    private BookingType bookingType = BookingType.ONLINE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

}
