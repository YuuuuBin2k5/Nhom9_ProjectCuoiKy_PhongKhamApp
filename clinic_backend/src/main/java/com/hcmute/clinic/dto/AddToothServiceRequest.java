package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a service to a specific tooth
 * Used when doctor clicks on a tooth and selects a service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToothServiceRequest {
    private Long serviceId;           // ID of the service to add
    private Integer sequenceOrder;    // Order of this step in the treatment plan
}
