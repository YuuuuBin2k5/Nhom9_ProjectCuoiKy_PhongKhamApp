package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
