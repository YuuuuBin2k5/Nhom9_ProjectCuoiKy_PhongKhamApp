package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu tạo mới tài khoản Bác sĩ.
 */
@Data
public class CreateDoctorRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String specialty;
    private String specialization; // Keep for backward compatibility
    private String licenseNumber;
    private Long clinicRoomId;
    private Integer experienceYears;
    private String bio;
}
