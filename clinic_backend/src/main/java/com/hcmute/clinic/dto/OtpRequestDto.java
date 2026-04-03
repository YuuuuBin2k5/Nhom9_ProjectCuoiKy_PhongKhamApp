package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu gửi mã xác thực OTP.
 */
@Data
public class OtpRequestDto {
    private String phone;
    private String email;
    /** "LOGIN", "REGISTER", hoặc "FORGOT_PASSWORD" */
    private String purpose;
}
