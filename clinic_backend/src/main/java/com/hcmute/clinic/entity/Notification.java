package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Lớp Thực thể Notification (Thông báo) - Lưu trữ các thông báo hệ thống gửi cho bệnh nhân.
 * Hỗ trợ các loại thông báo về Lịch hẹn (UC_02), Hóa đơn (UC_08) và nhắc nhở tái khám.
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String type;

    @Builder.Default
    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
