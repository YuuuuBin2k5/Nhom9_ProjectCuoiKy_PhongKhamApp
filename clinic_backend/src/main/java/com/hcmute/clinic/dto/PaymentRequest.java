package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO chứa thông tin yêu cầu thanh toán hóa đơn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String paymentMethod; // CASH, BANK_TRANSFER, etc.
    private BigDecimal amount;
    private String note;
}
