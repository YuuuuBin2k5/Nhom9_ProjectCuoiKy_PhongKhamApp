package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.*;
import com.hcmute.clinic.repository.*;
import com.hcmute.clinic.service.QueueEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lớp Dịch vụ InvoiceService (Dịch vụ Hóa đơn) - Thành phần chính xử lý logic thanh toán và quyết toán.
 * Chịu trách nhiệm tạo hóa đơn từ phác đồ, xử lý thanh toán và cập nhật trạng thái hệ thống sau khi thu phí.
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentPlanStepRepository treatmentPlanStepRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;
    private final CheckInQueueRepository checkInQueueRepository;
    private final QueueEventService queueEventService;
    private final com.hcmute.clinic.repository.AppointmentRepository appointmentRepository;
    
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
    
    /**
     * Xử lý nghiệp vụ thanh toán hóa đơn. 
     * Sau khi thanh toán thành công, sẽ tự động cập nhật trạng thái lịch hẹn và hàng đợi tương ứng.
     * @param invoiceId ID của hóa đơn.
     * @param request Thông tin thanh toán (phương thức, số tiền).
     * @param auth Đối tượng xác thực người dùng.
     * @return Phản hồi kết quả thanh toán.
     */
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
        
        // ====== MARK QUEUE AS COMPLETED AFTER PAYMENT ======
        // When payment is finalized, mark the patient's queue as COMPLETED
        // This ensures the check-in status is removed from patient dashboard
        try {
            if (invoice.getPatient() != null) {
                List<CheckInQueue> queuesForPatient = checkInQueueRepository.findTodayForPatient(
                    invoice.getPatient().getId(),
                    LocalDate.now().atStartOfDay(),
                    LocalDate.now().plusDays(1).atStartOfDay()
                );
                for (CheckInQueue q : queuesForPatient) {
                    if (q.getStatus() != QueueStatus.COMPLETED && q.getStatus() != QueueStatus.SKIPPED) {
                        q.setStatus(QueueStatus.COMPLETED);
                        q.setCompletedAt(LocalDateTime.now());
                        checkInQueueRepository.save(q);
                        if (q.getClinicRoom() != null) {
                            queueEventService.broadcastQueueUpdated(q.getClinicRoom().getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log but do not fail payment for queue errors
            System.err.println("[InvoiceService] Queue completion error after payment: " + e.getMessage());
        }
        // ====== END QUEUE COMPLETION ======

        // ====== SAFETY: Mark Appointment as COMPLETED on payment if still IN_PROGRESS ======
        try {
            com.hcmute.clinic.entity.Appointment appt = null;
            if (invoice.getTreatmentPlan() != null) {
                if (invoice.getTreatmentPlan().getMedicalRecord() != null
                        && invoice.getTreatmentPlan().getMedicalRecord().getAppointment() != null) {
                    appt = invoice.getTreatmentPlan().getMedicalRecord().getAppointment();
                } else if (invoice.getTreatmentPlan().getAppointment() != null) {
                    appt = invoice.getTreatmentPlan().getAppointment();
                }
            }
            if (appt != null && appt.getStatus() != com.hcmute.clinic.enums.AppointmentStatus.COMPLETED
                    && appt.getStatus() != com.hcmute.clinic.enums.AppointmentStatus.CANCELLED) {
                appt.setStatus(com.hcmute.clinic.enums.AppointmentStatus.COMPLETED);
                appointmentRepository.save(appt);
            }
        } catch (Exception e) {
            System.err.println("[InvoiceService] Safety-mark appointment COMPLETED on payment error: " + e.getMessage());
        }
        // ====== END SAFETY ======
        
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
    /**
     * Nghiệp vụ tạo hóa đơn từ phác đồ điều trị. 
     * Tổng hợp tất cả các bước điều trị đã hoàn tất để tính toán tổng chi phí.
     * @param treatmentPlanId ID của phác đồ điều trị.
     * @return Đối tượng Invoice đã được lưu.
     */
    @Transactional
    public Invoice createInvoiceFromTreatmentPlan(Long treatmentPlanId) {
        // 1. Load treatment plan with steps
        TreatmentPlan plan = treatmentPlanRepository.findById(treatmentPlanId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phác đồ điều trị không tồn tại"));
        
        // 2. Instead of rejecting, auto-SKIP any remaining PENDING/IN_PROGRESS steps
        // This allows the doctor to manually complete the treatment without doing every step
        for (TreatmentPlanStep step : plan.getSteps()) {
            if (step.getStatus() == StepStatus.PENDING || step.getStatus() == StepStatus.IN_PROGRESS) {
                step.setStatus(StepStatus.SKIPPED);
                treatmentPlanStepRepository.save(step);
            }
        }
        // Reload plan steps for invoice calculation
        plan.getSteps().forEach(s -> {}); // trigger lazy load
        
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
                // Ưu tiên actualPrice do bác sĩ nhập (tính từ phần kê đơn theo dịch vụ),
                // fallback sang estimated price của service nếu actualPrice chưa có.
                BigDecimal price = step.getActualPrice() != null ? step.getActualPrice() : step.getService().getPrice();
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

        // 7b. Mark related Appointment as COMPLETED so patient can book again
        try {
            if (plan.getMedicalRecord() != null && plan.getMedicalRecord().getAppointment() != null) {
                com.hcmute.clinic.entity.Appointment appt = plan.getMedicalRecord().getAppointment();
                if (appt.getStatus() != com.hcmute.clinic.enums.AppointmentStatus.COMPLETED
                        && appt.getStatus() != com.hcmute.clinic.enums.AppointmentStatus.CANCELLED) {
                    appt.setStatus(com.hcmute.clinic.enums.AppointmentStatus.COMPLETED);
                    appointmentRepository.save(appt);
                }
            }
        } catch (Exception e) {
            System.err.println("[InvoiceService] Failed to mark appointment COMPLETED: " + e.getMessage());
        }
        
        // ====== QUEUE FLUSH: Remove patient from today's queue ======
        // This ensures the patient exits the waiting room after completing treatment.
        try {
            List<CheckInQueue> queuesForPatient = checkInQueueRepository.findTodayForPatient(
                plan.getPatient().getId(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
            );
            for (CheckInQueue q : queuesForPatient) {
                if (q.getStatus() == QueueStatus.IN_PROGRESS ||
                    q.getStatus() == QueueStatus.WAITING ||
                    q.getStatus() == QueueStatus.RETURNED_PRIORITY) {
                    q.setStatus(QueueStatus.COMPLETED);
                    q.setCompletedAt(LocalDateTime.now());
                    checkInQueueRepository.save(q);
                    if (q.getClinicRoom() != null) {
                        queueEventService.broadcastQueueUpdated(q.getClinicRoom().getId());
                    }
                }
            }
        } catch (Exception e) {
            // Log but do not fail invoice creation for queue errors
            System.err.println("[InvoiceService] Queue flush error: " + e.getMessage());
        }
        // ====== END QUEUE FLUSH ======
        
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
            .createdAt(invoice.getCreatedAt())
            .prescriptionId(invoice.getTreatmentPlan() != null 
                    && invoice.getTreatmentPlan().getMedicalRecord() != null
                    && invoice.getTreatmentPlan().getMedicalRecord().getPrescription() != null
                ? invoice.getTreatmentPlan().getMedicalRecord().getPrescription().getId()
                : null)
            .diagnosis(invoice.getTreatmentPlan() != null && invoice.getTreatmentPlan().getMedicalRecord() != null
                ? invoice.getTreatmentPlan().getMedicalRecord().getDiagnosis()
                : null)
            .advice(invoice.getTreatmentPlan() != null && invoice.getTreatmentPlan().getMedicalRecord() != null
                ? invoice.getTreatmentPlan().getMedicalRecord().getAdvice()
                : null)
            .items(itemDtos)
            .prescriptionDetails(invoice.getTreatmentPlan() != null 
                    && invoice.getTreatmentPlan().getMedicalRecord() != null
                    && invoice.getTreatmentPlan().getMedicalRecord().getPrescription() != null
                ? invoice.getTreatmentPlan().getMedicalRecord().getPrescription().getDetails().stream()
                    .map(d -> com.hcmute.clinic.dto.PrescriptionDTO.DetailDTO.builder()
                        .id(d.getId())
                        .treatmentPlanStepId(d.getTreatmentPlanStepId())
                        .medicineName(d.getMedicineName())
                        .dosage(d.getDosage())
                        .frequency(d.getFrequency())
                        .duration(d.getDuration())
                        .unit(d.getUnit())
                        .build())
                    .collect(Collectors.toList())
                : Collections.emptyList())
            .build();
    }
}
