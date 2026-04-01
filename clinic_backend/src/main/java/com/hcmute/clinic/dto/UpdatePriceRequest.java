package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Request DTO for updating the price of a treatment plan step
 * Used when doctor needs to adjust the price of a service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceRequest {
    private BigDecimal newPrice;  // New price for the step
}
