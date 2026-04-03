package com.hcmute.clinic.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO yêu cầu tạo hoặc cập nhật thông tin phòng khám.
 */
@Data
public class RoomRequest {
    @NotBlank
    private String name;
    
    private String description;
}
