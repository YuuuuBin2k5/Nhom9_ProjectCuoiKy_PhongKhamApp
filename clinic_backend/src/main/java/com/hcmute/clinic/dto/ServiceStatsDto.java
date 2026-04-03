package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO Thống kê hiệu suất kinh doanh của từng dịch vụ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatsDto {
    private Long serviceId;
    private String serviceName;
    private Integer totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private Integer totalReviews;
}
