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
    /** Phác đồ gắn với hóa đơn (sau khi hoàn tất điều trị). */
    private Long treatmentPlanId;
    private String patientName;
    private BigDecimal totalAmount;
    /** UNPAID / PARTIAL / PAID / CANCELLED — tương đương trạng thái chờ thanh toán khi chưa PAID. */
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
        private String toothNumber;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String description;
    }
}
