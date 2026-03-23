package com.hcmute.clinic.dto;

import lombok.Data;

@Data
public class OtpVerifyDto {
    private String phone;
    private String code;
    private String purpose;
}
