package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CheckInScanRequest;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.security.JwtService;
import com.hcmute.clinic.service.CheckInQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CheckInController - API Controller xử lý Check-in.
 * Cung cấp endpoint cho flow SE_06: Quét mã QR để ghi nhận bệnh nhân vào hàng đợi.
 */
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInQueueService checkInQueueService;
    private final JwtService jwtService;
    private final PatientRepository patientRepository;

    /**
     * Endpoint SE_06: Quét mã QR từ máy quét tại quầy lễ tân.
     */
    @PostMapping("/scan")
    public ResponseEntity<?> scan(@RequestBody CheckInScanRequest request) {
        try {
            var result = checkInQueueService.processScan(request.getQrData());
            return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "queueNumber", result.queueNumber(),
                "queuePosition", result.queuePosition(),
                "roomName", result.roomName(),
                "roomLocation", result.roomLocation() != null ? result.roomLocation() : "",
                "message", result.message(),
                "alreadyCheckedIn", result.alreadyCheckedIn(),
                "estimatedWaitTime", result.estimatedWaitTime()
            ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("success", false, "message", e.getReason() != null ? e.getReason() : "Lỗi"));
        }
    }

    @PostMapping("/self-scan")
    public ResponseEntity<?> selfCheckIn(Authentication auth, @RequestBody CheckInScanRequest request) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        long patientId = Long.parseLong(auth.getName());
        
        try {
            var result = checkInQueueService.processSelfScan(patientId, request.getQrData());
            return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "queueNumber", result.queueNumber(),
                "queuePosition", result.queuePosition(),
                "roomName", result.roomName(),
                "roomLocation", result.roomLocation() != null ? result.roomLocation() : "",
                "message", result.message(),
                "alreadyCheckedIn", result.alreadyCheckedIn(),
                "estimatedWaitTime", result.estimatedWaitTime()
            ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("success", false, "message", e.getReason() != null ? e.getReason() : "Lỗi"));
        }
    }

    @GetMapping("/qr-token")
    public ResponseEntity<?> getQrToken(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        long patientId = Long.parseLong(auth.getName());
        try {
            com.hcmute.clinic.dto.GenerateCheckInQRRequest req = new com.hcmute.clinic.dto.GenerateCheckInQRRequest();
            req.setPatientId(patientId);
            var res = checkInQueueService.generateCheckInQR(req);
            return ResponseEntity.ok(Map.of(
                "token", res.getQrData(),
                "expiresIn", 86400,
                "expiresAt", res.getExpiresAt()
            ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("success", false, "message", e.getReason() != null ? e.getReason() : "Lỗi"));
        }
    }
}
