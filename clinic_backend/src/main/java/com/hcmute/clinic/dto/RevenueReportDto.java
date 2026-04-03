package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO chứa dữ liệu báo cáo doanh thu và thống kê hoạt động phòng khám.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDto {
    private Integer year;
    private Integer month;
    private BigDecimal totalRevenue;
    private Integer totalAppointments;
    private Integer completedAppointments;
    private Integer cancelledAppointments;
    private BigDecimal averageRevenuePerAppointment;
    private List<RevenueCategoryDTO> categories;
}
