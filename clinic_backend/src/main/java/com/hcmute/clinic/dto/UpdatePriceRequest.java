package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO yêu cầu cập nhật giá tiền cho một bước điều trị cụ thể.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceRequest {
    private BigDecimal newPrice;  // New price for the step
}
