package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.RevenueReportDto;
import com.hcmute.clinic.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý và truy xuất báo cáo doanh thu nhanh cho Admin.
 */
@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
public class AdminRevenueController {

    private final RevenueService revenueService;

    /**
     * Lấy báo cáo doanh thu dựa trên thời gian tùy chọn.
     *
     * @param year  Năm cần lấy báo cáo (tùy chọn).
     * @param month Tháng cần lấy báo cáo (tùy chọn).
     * @param day   Ngày cần lấy báo cáo (tùy chọn).
     * @return ResponseEntity chứa đối tượng RevenueReportDto.
     */
    @GetMapping("/report")
    public ResponseEntity<RevenueReportDto> getRevenueReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {
            
        return ResponseEntity.ok(revenueService.getRevenueReport(year, month, day));
    }
}
