package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO phản hồi thông tin hồ sơ của bệnh nhân đang đăng nhập.
 */
@Data
@Builder
public class PatientMeResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String qrCodeData;
    private String phone;
    private String address;
    private String gender;
    private String dob;
    private String avatarUrl;
    private String bloodType;
    private String allergies;
    private String underlyingConditions;
}
