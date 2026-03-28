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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    
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
