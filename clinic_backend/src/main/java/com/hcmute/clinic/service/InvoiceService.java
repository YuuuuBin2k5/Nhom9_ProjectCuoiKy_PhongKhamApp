package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.*;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;
    
    public List<InvoiceDto> getPatientInvoices(Long patientId) {
        return invoiceRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public InvoiceDto getInvoiceDetail(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        return toDto(invoice);
    }
    
    @Transactional
    public PaymentResponse processPayment(Long invoiceId, PaymentRequest request, Authentication auth) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        
        if (invoice.getPaymentStatus() == InvoiceStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice already paid");
        }
        
        // Update invoice
        invoice.setPaymentStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setPaidBy(auth.getName());
        invoice.setPaidAmount(request.getAmount());
        invoice.setRemainingAmount(invoice.getTotalAmount().subtract(request.getAmount()));
        
        invoiceRepository.save(invoice);
        
        return PaymentResponse.builder()
            .success(true)
            .message("Payment successful")
            .invoiceId(invoice.getId())
            .paymentStatus("PAID")
            .paidAt(invoice.getPaidAt())
            .build();
    }
    
    /**
     * Tạo Invoice từ Treatment Plan đã hoàn tất
     * Tổng hợp tất cả steps COMPLETED và tạo hóa đơn
     */
    @Transactional
    public Invoice createInvoiceFromTreatmentPlan(Long treatmentPlanId) {
        // 1. Load treatment plan with steps
        TreatmentPlan plan = treatmentPlanRepository.findById(treatmentPlanId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phác đồ điều trị không tồn tại"));
        
        // 2. Validate: All steps must be COMPLETED or SKIPPED
        boolean hasIncompleteSteps = plan.getSteps().stream()
            .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
        
        if (hasIncompleteSteps) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Không thể tạo hóa đơn. Vui lòng hoàn thành tất cả các bước điều trị trước.");
        }
        
        // 3. Check if invoice already exists
        Optional<Invoice> existingInvoice = invoiceRepository.findByTreatmentPlanId(treatmentPlanId);
        if (existingInvoice.isPresent()) {
            return existingInvoice.get(); // Return existing invoice
        }
        
        // 4. Calculate total amount from completed steps
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();
        
        for (TreatmentPlanStep step : plan.getSteps()) {
            if (step.getStatus() == StepStatus.COMPLETED && step.getService() != null) {
                BigDecimal price = step.getService().getPrice();
                if (price != null) {
                    totalAmount = totalAmount.add(price);
                    
                    InvoiceItem item = InvoiceItem.builder()
                        .service(step.getService())
                        .treatmentPlanStep(step)
                        .serviceName(step.getService().getName())
                        .toothNumber(step.getToothNumber())
                        .quantity(1)
                        .unitPrice(price)
                        .totalPrice(price)
                        .description(step.getDoctorConclusion())
                        .build();
                    items.add(item);
                }
            }
        }
        
        // 5. Create invoice
        Invoice invoice = Invoice.builder()
            .patient(plan.getPatient())
            .treatmentPlan(plan)
            .medicalRecord(plan.getMedicalRecord())
            .totalAmount(totalAmount)
            .remainingAmount(totalAmount)
            .paymentStatus(InvoiceStatus.UNPAID)
            .build();
        
        invoice = invoiceRepository.save(invoice);
        
        // 6. Save invoice items
        for (InvoiceItem item : items) {
            item.setInvoice(invoice);
        }
        invoice.setItems(items);
        invoiceRepository.save(invoice);
        
        // 7. Mark treatment plan as COMPLETED
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        treatmentPlanRepository.save(plan);
        
        // 8. Send notification to patient
        Notification notif = Notification.builder()
            .patient(plan.getPatient())
            .title("💰 Hóa đơn thanh toán")
            .message(String.format(
                "Phác đồ điều trị của bạn đã hoàn tất!\n\n" +
                "Tổng chi phí: %,.0f VNĐ\n" +
                "Vui lòng thanh toán tại quầy lễ tân hoặc qua ứng dụng.",
                totalAmount.doubleValue()
            ))
            .type("INVOICE_CREATED")
            .build();
        notificationRepository.save(notif);
        
        if (plan.getPatient().getFcmToken() != null && !plan.getPatient().getFcmToken().isBlank()) {
            try {
                fcmService.sendNotification(
                    plan.getPatient().getFcmToken(), 
                    notif.getTitle(), 
                    notif.getMessage()
                );
            } catch (Exception e) {
                // Log error but don't fail invoice creation
                System.err.println("Failed to send FCM notification: " + e.getMessage());
            }
        }
        
        return invoice;
    }
    
    private InvoiceDto toDto(Invoice invoice) {
        return InvoiceDto.builder()
            .id(invoice.getId())
            .patientId(invoice.getPatient().getId())
            .patientName(invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName())
            .totalAmount(invoice.getTotalAmount())
            .paymentStatus(invoice.getPaymentStatus().toString())
            .paymentMethod(invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().toString() : null)
            .paidAt(invoice.getPaidAt())
            .createdAt(invoice.getCreatedAt())
            .build();
    }
}
