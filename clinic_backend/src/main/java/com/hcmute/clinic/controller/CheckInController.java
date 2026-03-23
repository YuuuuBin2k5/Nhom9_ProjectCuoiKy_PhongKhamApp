package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CheckInScanRequest;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.security.JwtService;
import com.hcmute.clinic.service.CheckInQueueService;
import com.hcmute.clinic.service.ScanLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInQueueService checkInQueueService;
    private final ScanLogService scanLogService;
    private final JwtService jwtService;
    private final PatientRepository patientRepository;

    @PostMapping("/scan")
    public ResponseEntity<?> scan(@RequestBody CheckInScanRequest request) {
        try {
            var result = checkInQueueService.processScan(request.getQrData());
            return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "queueNumber", result.queueNumber(),
                "roomName", result.roomName(),
                "roomLocation", result.roomLocation() != null ? result.roomLocation() : "",
                "message", result.message(),
                "alreadyCheckedIn", result.alreadyCheckedIn()
            ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            try {
                scanLogService.logFailedScan(
                        request != null ? request.getQrData() : null,
                        e.getStatusCode(),
                        e.getReason() != null ? e.getReason() : "Lỗi"
                );
            } catch (Exception ex) { /* ignore logging errors */ }
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("success", false, "message", e.getReason() != null ? e.getReason() : "Lỗi"));
        }
    }

    @GetMapping("/qr-token")
    public ResponseEntity<?> getQrToken(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        if (!patientRepository.existsById(patientId)) {
            return ResponseEntity.notFound().build();
        }
        String token = jwtService.generateQrToken(String.valueOf(patientId));
        return ResponseEntity.ok(Map.of("token", token, "expiresIn", 180));
    }
}
