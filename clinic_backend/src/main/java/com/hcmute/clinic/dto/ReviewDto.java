package com.hcmute.clinic.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO đại diện cho một đánh giá dịch vụ từ bệnh nhân.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long serviceId;
    private String serviceName;
    private Long appointmentId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
