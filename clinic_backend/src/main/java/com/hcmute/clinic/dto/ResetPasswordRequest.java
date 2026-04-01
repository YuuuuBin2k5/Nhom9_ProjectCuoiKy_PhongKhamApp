package com.hcmute.clinic.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String phone;
    private String email;
    private String otp;
    private String newPassword;
}
