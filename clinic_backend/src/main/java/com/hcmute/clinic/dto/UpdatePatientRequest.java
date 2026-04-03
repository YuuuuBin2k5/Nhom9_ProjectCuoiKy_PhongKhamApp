package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu cập nhật hồ sơ bệnh nhân.
 */
@Data
public class UpdatePatientRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String gender;
    private String dob; // ISO date string: "1995-06-15"
    private String avatarUrl;
    private String allergies;
    private String underlyingConditions;
    private String bloodType;
}
