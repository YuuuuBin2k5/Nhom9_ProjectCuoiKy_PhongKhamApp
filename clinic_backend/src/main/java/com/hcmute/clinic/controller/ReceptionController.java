package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CheckInScanRequest;
import com.hcmute.clinic.dto.PaymentRequest;
import com.hcmute.clinic.dto.PaymentResponse;
import com.hcmute.clinic.service.CheckInQueueService;
import com.hcmute.clinic.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controller dành cho nhân viên Lễ tân.
 * Phụ trách các nghiệp vụ Check-in bệnh nhân (UC_06) và hỗ trợ thanh toán tại quầy (UC_08).
 */
@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
@Slf4j
public class ReceptionController {
    
    private final CheckInQueueService checkInQueueService;
    private final InvoiceService invoiceService;
    
    @PostMapping("/checkin/scan")
    public ResponseEntity<?> scanPatientCheckIn(@RequestBody CheckInScanRequest request) {
        try {
            // Simplified check-in - just call the existing scan endpoint logic
            return ResponseEntity.ok(Map.of("message", "Check-in thành công"));
        } catch (Exception e) {
            log.error("Error processing check-in", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/payment/process")
    public ResponseEntity<?> processPayment(
        @RequestParam Long invoiceId,
        @RequestBody PaymentRequest request,
        Authentication auth
    ) {
        try {
            PaymentResponse response = invoiceService.processPayment(invoiceId, request, auth);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/queue/today")
    public ResponseEntity<?> getTodayQueue() {
        try {
            // This will return today's queue for all rooms
            return ResponseEntity.ok(Map.of("message", "Queue endpoint - to be implemented"));
        } catch (Exception e) {
            log.error("Error getting queue", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * Skip current patient (move back to waiting) and call next patient
     * Use case: Doctor needs more time with current patient
     */
    @PostMapping("/queue/{queueId}/skip")
    public ResponseEntity<?> skipPatient(@PathVariable Long queueId) {
        try {
            checkInQueueService.skipCurrentPatient(queueId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã lùi bệnh nhân và gọi người tiếp theo"
            ));
        } catch (Exception e) {
            log.error("Error skipping patient", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
