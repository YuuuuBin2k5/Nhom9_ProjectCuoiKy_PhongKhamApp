package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for adding a service to multiple teeth at once
 * Used for services like crown, extraction that can be applied to multiple teeth
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMultipleTeethServiceRequest {
    private Long serviceId;
    private List<String> toothNumbers;  // List of tooth numbers (e.g., ["11", "12", "21"])
    private Integer startingSequenceOrder;
    private String notes;  // Optional notes (e.g., crown type, special instructions)
    private BigDecimal customPrice;  // Optional custom price (e.g., for crown types with different prices)
}
