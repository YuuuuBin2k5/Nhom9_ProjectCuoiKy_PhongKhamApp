package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Lớp Thực thể OtpChallenge (Yêu cầu xác thực OTP) - Lưu trữ các thử thách OTP đã phát hành.
 * Dùng để kiểm soát thời gian hết hạn và số lần thử sai của mã xác thực.
 */
@Entity
@Table(name = "otp_challenges", indexes = {
        @Index(name = "idx_otp_phone_purpose", columnList = "phone_e164,purpose"),
        @Index(name = "idx_otp_email_purpose", columnList = "email,purpose")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_e164", length = 20)
    private String phoneE164;

    @Column(name = "email", length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OtpPurpose purpose;

    @Column(name = "code_hash", nullable = false, length = 120)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean consumed = false;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
