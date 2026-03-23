package com.hcmute.clinic.dto;

import lombok.Data;

@Data
public class OtpRequestDto {
    private String phone;
    /** "LOGIN" hoặc "REGISTER" */
    private String purpose;
}
