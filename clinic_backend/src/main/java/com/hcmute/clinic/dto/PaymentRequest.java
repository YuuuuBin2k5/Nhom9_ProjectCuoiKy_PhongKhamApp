package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String paymentMethod; // CASH, BANK_TRANSFER, etc.
    private BigDecimal amount;
    private String note;
}
