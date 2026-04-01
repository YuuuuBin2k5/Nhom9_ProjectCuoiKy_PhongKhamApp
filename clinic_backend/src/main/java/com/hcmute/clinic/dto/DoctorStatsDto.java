package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

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
