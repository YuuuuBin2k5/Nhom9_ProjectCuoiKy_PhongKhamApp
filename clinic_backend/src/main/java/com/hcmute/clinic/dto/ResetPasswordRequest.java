package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu đặt lại mật khẩu bằng mã OTP.
 */
@Data
public class ResetPasswordRequest {
    private String phone;
    private String email;
    private String otp;
    private String newPassword;
}
