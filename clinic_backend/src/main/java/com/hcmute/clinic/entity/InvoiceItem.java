package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Lớp Thực thể InvoiceItem (Chi tiết hóa đơn) - Đại diện cho một dòng (line item) trong hóa đơn.
 * Mỗi item thường tương ứng với một dịch vụ hoặc thuốc đã được sử dụng.
 */
@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {
    
    // Thuộc tính private (Encapsulation).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Hóa đơn cha chứa mục này.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Invoice invoice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_step_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TreatmentPlanStep treatmentPlanStep;
    
    @Column(name = "service_name", nullable = false)
    private String serviceName;
    
    @Column(name = "tooth_number")
    private String toothNumber;
    
    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
