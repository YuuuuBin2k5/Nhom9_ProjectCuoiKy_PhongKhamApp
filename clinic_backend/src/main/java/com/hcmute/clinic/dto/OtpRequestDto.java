package com.hcmute.clinic.dto;

import lombok.Data;

@Data
public class OtpRequestDto {
    private String phone;
    private String email;
    /** "LOGIN", "REGISTER", hoặc "FORGOT_PASSWORD" */
    private String purpose;
}
