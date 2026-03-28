package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

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
