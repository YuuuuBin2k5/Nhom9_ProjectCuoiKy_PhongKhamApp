package com.hcmute.clinic.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String message;
    private Long invoiceId;
    private String paymentStatus;
    private LocalDateTime paidAt;
}
