package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO mô tả thông tin chi tiết của Bác sĩ.
 * Thường dùng để hiển thị hồ sơ bác sĩ cho bệnh nhân hoặc quản trị viên.
 */
@Data
@Builder
public class DoctorDetailDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String roomName;
    private Integer experienceYears;
    private String biography;
    private String avatarUrl;
    private long appointmentCount;
}
