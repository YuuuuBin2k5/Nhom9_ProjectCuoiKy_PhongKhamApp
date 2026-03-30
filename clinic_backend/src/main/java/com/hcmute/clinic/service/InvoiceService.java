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
import java.util.Collections;
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
    
    @Transactional(readOnly = true)
    public List<InvoiceDto> getPatientInvoices(Long patientId) {
        List<Invoice> invoices = invoiceRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        for (Invoice inv : invoices) {
            if (inv.getTreatmentPlan() != null) {
                inv.getTreatmentPlan().getId();
            }
            if (inv.getItems() != null) {
                inv.getItems().size();
            }
        }
        return invoices.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceDetail(Long id, Authentication auth) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        assertPatientOwnsInvoiceIfPatientRole(invoice, auth);
        if (invoice.getItems() != null) {
            invoice.getItems().size();
        }
        return toDto(invoice);
    }
    
    @Transactional
    public PaymentResponse processPayment(Long invoiceId, PaymentRequest request, Authentication auth) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        
        assertPatientOwnsInvoiceIfPatientRole(invoice, auth);
        
        if (invoice.getPaymentStatus() == InvoiceStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice already paid");
        }
        
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không hợp lệ");
        }
        
        // UC_08: cổng thanh toán bên thứ ba — ở môi trường demo coi như gọi gateway thành công (tích hợp thật: redirect + callback).
        if (method != PaymentMethod.CASH) {
            // Gateway: VNPay / MoMo / Banking / …
        }
        
        // Update invoice
        invoice.setPaymentStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(method);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setPaidBy(auth != null ? auth.getName() : null);
        invoice.setPaidAmount(request.getAmount());
        BigDecimal remaining = invoice.getTotalAmount().subtract(request.getAmount());
        invoice.setRemainingAmount(remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining);
        
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
    
    private void assertPatientOwnsInvoiceIfPatientRole(Invoice invoice, Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return;
        }
        boolean isPatient = auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()));
        if (!isPatient) {
            return;
        }
        long patientId = Long.parseLong(auth.getName());
        if (!invoice.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập hóa đơn này");
        }
    }
    
    private InvoiceDto toDto(Invoice invoice) {
        Long treatmentPlanId = invoice.getTreatmentPlan() != null ? invoice.getTreatmentPlan().getId() : null;
        List<InvoiceDto.InvoiceItemDto> itemDtos = Collections.emptyList();
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            itemDtos = invoice.getItems().stream()
                .map(i -> InvoiceDto.InvoiceItemDto.builder()
                    .serviceName(i.getServiceName())
                    .toothNumber(i.getToothNumber())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .totalPrice(i.getTotalPrice())
                    .description(i.getDescription())
                    .build())
                .collect(Collectors.toList());
        }
        return InvoiceDto.builder()
            .id(invoice.getId())
            .patientId(invoice.getPatient().getId())
            .treatmentPlanId(treatmentPlanId)
            .patientName(invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName())
            .totalAmount(invoice.getTotalAmount())
            .paymentStatus(invoice.getPaymentStatus().toString())
            .paymentMethod(invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().toString() : null)
            .paidAt(invoice.getPaidAt())
            .createdAt(invoice.getCreatedAt())
            .items(itemDtos)
            .build();
    }
}
