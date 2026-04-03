package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.QueueStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Lớp Thực thể CheckInQueue (Hàng chờ check-in) - Thành phần chính của flow SE_06 (Xếp hàng khám).
 * Quản lý thứ tự bệnh nhân đang chờ tại phòng khám, hỗ trợ điều phối luồng làm việc.
 */
@Entity
@Table(name = "check_in_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInQueue {
    // Encapsulation: Chế độ truy cập private cho tất cả các trường.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "clinic_room_id", nullable = false)
    private ClinicRoom clinicRoom;

    @Column(name = "original_room_id")
    private Long originalRoomId;

    @OneToOne
    @JoinColumn(name = "treatment_plan_step_id")
    private TreatmentPlanStep treatmentPlanStep;

    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private QueueStatus status = QueueStatus.WAITING;

    @Builder.Default
    @Column(name = "priority_level")
    private Integer priorityLevel = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}
