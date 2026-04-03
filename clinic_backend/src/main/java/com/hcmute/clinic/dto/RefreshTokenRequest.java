package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu làm mới Token truy cập.
 */
@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
