package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.RevenueReportDTO;
import com.hcmute.clinic.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
public class AdminRevenueController {

    private final RevenueService revenueService;

    @GetMapping("/report")
    public ResponseEntity<RevenueReportDTO> getRevenueReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {
            
        return ResponseEntity.ok(revenueService.getRevenueReport(year, month, day));
    }
}
