package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scan_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã QR đã quét (có thể truncate nếu quá dài - JWT) */
    @Column(name = "qr_data", length = 512)
    private String qrData;

    /** Mã lỗi HTTP: 400, 404, 409, ... */
    @Column(name = "status_code")
    private Integer statusCode;

    /** Thông báo lỗi hiển thị cho người dùng */
    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
