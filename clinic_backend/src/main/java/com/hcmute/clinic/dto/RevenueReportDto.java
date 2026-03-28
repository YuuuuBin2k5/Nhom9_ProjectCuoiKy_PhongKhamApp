package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDto {
    private Integer year;
    private Integer month;
    private BigDecimal totalRevenue;
    private Integer totalAppointments;
    private Integer completedAppointments;
    private Integer cancelledAppointments;
    private BigDecimal averageRevenuePerAppointment;
}
