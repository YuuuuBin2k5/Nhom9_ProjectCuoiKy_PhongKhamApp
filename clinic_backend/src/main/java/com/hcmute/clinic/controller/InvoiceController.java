package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Lớp Điều khiển InvoiceController - Quản lý các yêu cầu liên quan đến Hóa đơn và Thanh toán.
 * Cung cấp API cho bệnh nhân xem hóa đơn và thực hiện thanh toán (UC_08).
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<InvoiceDto>> getMyInvoices(Authentication auth) {
        Long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(invoiceService.getPatientInvoices(patientId));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('RECEPTIONIST') or hasRole('ADMIN')")
    public ResponseEntity<InvoiceDto> getInvoiceDetail(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(invoiceService.getInvoiceDetail(id, auth));
    }
    
    /**
     * API thực hiện thanh toán hóa đơn (UC_08).
     * Chuyển trạng thái hóa đơn sang Đã thanh toán và kích hoạt các logic nghiệp vụ đi kèm.
     */
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('PATIENT') or hasRole('RECEPTIONIST')")
    public ResponseEntity<PaymentResponse> processPayment(
        @PathVariable Long id,
        @RequestBody PaymentRequest request,
        Authentication auth
    ) {
        return ResponseEntity.ok(invoiceService.processPayment(id, request, auth));
    }
}
