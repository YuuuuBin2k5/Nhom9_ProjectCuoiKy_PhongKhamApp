package com.hcmute.clinic.dto;

import lombok.*;

/**
 * DTO chứa thông tin yêu cầu tạo mới đánh giá dịch vụ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Long appointmentId;
    private Long doctorId;
    private Long serviceId;
    private Integer rating; // 1-5
    private String comment;
}
