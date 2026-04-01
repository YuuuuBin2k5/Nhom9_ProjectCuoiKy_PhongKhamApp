package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a general service (not specific to a tooth)
 * Used for services like consultation, X-ray, teeth cleaning, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGeneralServiceRequest {
    private Long serviceId;           // ID of the service to add
    private Integer sequenceOrder;    // Order of this step in the treatment plan
}
