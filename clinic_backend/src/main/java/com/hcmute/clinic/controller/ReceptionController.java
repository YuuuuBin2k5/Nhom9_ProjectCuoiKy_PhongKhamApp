package com.hcmute.clinic.controller;

import com.hcmute.clinic.service.ScanLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
public class ReceptionController {

    private final ScanLogService scanLogService;

    @GetMapping("/scan-logs")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<?> getScanLogs(@RequestParam(defaultValue = "50") int limit) {
        List<ScanLogService.ScanLogDto> logs = scanLogService.getRecentErrors(Math.min(limit, 100));
        return ResponseEntity.ok(Map.of("items", logs));
    }
}
