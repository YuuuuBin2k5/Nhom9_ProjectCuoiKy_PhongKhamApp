package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO Phản hồi khi thêm một dịch vụ cho một răng cụ thể thành công.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToothServiceResponse {
    private Long stepId;              // ID of the created TreatmentPlanStep
    private String toothNumber;       // Tooth number (FDI notation: "8", "16", etc.)
    private String serviceName;       // Name of the service
    private BigDecimal price;         // Price of the service
    private BigDecimal totalPlanCost; // Updated total cost of the treatment plan
}
