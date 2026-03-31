package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDTO {
    private BigDecimal totalRevenue;
    private List<RevenueCategoryDTO> categories;
}
