package com.hcmute.clinic.entity;

import com.hcmute.clinic.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp Thực thể Invoice (Hóa đơn) - Kết quả cuối cùng của flow SE_15 (Thanh toán).
 * Lưu trữ thông tin tài chính, tổng tiền, trạng thái thanh toán và liên kết với bệnh nhân, bác sĩ.
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    // Encapsulation: Đảm bảo tính đóng gói với phạm vi truy cập private.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "treatment_plan_id")
    private TreatmentPlan treatmentPlan;

    @OneToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "paid_amount")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private InvoiceStatus paymentStatus = InvoiceStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private com.hcmute.clinic.enums.PaymentMethod paymentMethod;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "paid_by")
    private String paidBy;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<Payment> payments;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<InvoiceItem> items;

}
