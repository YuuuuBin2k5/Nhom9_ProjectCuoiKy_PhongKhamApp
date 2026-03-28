package com.hcmute.clinic.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private List<InvoiceItemDto> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItemDto {
        private String serviceName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
