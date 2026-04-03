package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO Thống kê hiệu suất của Bác sĩ.
 * Chứa các chỉ số về doanh thu, lượt hẹn và đánh giá phục vụ báo cáo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsDto {
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private Integer totalAppointments;
    private Integer completedAppointments;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private Integer totalReviews;
}
