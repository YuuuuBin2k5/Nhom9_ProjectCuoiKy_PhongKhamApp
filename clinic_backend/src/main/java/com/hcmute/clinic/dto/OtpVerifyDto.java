package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa mã OTP và thông tin cần thiết để xác thực.
 */
@Data
public class OtpVerifyDto {
    private String phone;
    private String code;
    private String purpose;
}
