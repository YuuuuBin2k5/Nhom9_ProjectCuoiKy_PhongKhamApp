package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Response DTO when a general service is added
 * Contains information about the added service and updated plan cost
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralServiceResponse {
    private Long stepId;              // ID of the created TreatmentPlanStep
    private String serviceName;       // Name of the service
    private BigDecimal price;         // Price of the service
    private BigDecimal totalPlanCost; // Updated total cost of the treatment plan
}
