package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    
    private final AdminReportService adminReportService;
    
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportDto> getRevenueReport(
        @RequestParam int year,
        @RequestParam int month
    ) {
        return ResponseEntity.ok(adminReportService.getRevenueReport(year, month));
    }
    
    @GetMapping("/top-services")
    public ResponseEntity<List<ServiceStatsDto>> getTopServices(
        @RequestParam int year,
        @RequestParam int month,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(adminReportService.getTopServices(year, month, limit));
    }
    
    @GetMapping("/doctor-performance")
    public ResponseEntity<List<DoctorStatsDto>> getDoctorPerformance(
        @RequestParam int year,
        @RequestParam int month
    ) {
        return ResponseEntity.ok(adminReportService.getDoctorPerformance(year, month));
    }
}
