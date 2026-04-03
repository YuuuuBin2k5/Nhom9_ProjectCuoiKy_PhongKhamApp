package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về kết quả xác thực người dùng.
 * Bao gồm Token truy cập (JWT) và thông tin định danh cơ bản.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
    private Long userId;
    private String refreshToken;
}
