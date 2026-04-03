package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller cung cấp các báo cáo thống kê dành cho Quản trị viên.
 * Bao gồm báo cáo doanh thu, hiệu suất làm việc của bác sĩ và mức độ sử dụng dịch vụ.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    
    private final AdminReportService adminReportService;
    
    /**
     * Lấy báo cáo doanh thu - hỗ trợ cả khoảng thời gian và tham số năm/tháng
     * Ưu tiên: khoảng thời gian > năm/tháng > tháng hiện tại
     */
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportDto> getRevenueReport(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        // Priority 1: Date range (for frontend compatibility)
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(adminReportService.getRevenueReportByDateRange(startDate, endDate));
        }
        
        // Priority 2: Year and month (backward compatibility)
        if (year != null && month != null) {
            return ResponseEntity.ok(adminReportService.getRevenueReport(year, month));
        }
        
        // Priority 3: Default to current month
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(adminReportService.getRevenueReport(now.getYear(), now.getMonthValue()));
    }
    
    /**
     * Get top services - supports both date range and year/month parameters
     */
    @GetMapping("/top-services")
    public ResponseEntity<List<ServiceStatsDto>> getTopServices(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
        @RequestParam(defaultValue = "10") int limit
    ) {
        // Priority 1: Date range
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(adminReportService.getTopServicesByDateRange(startDate, endDate, limit));
        }
        
        // Priority 2: Year and month
        if (year != null && month != null) {
            return ResponseEntity.ok(adminReportService.getTopServices(year, month, limit));
        }
        
        // Priority 3: Default to current month
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(adminReportService.getTopServices(now.getYear(), now.getMonthValue(), limit));
    }
    
    /**
     * Get doctor performance - supports both date range and year/month parameters
     */
    @GetMapping("/doctor-performance")
    public ResponseEntity<List<DoctorStatsDto>> getDoctorPerformance(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        // Priority 1: Date range
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(adminReportService.getDoctorPerformanceByDateRange(startDate, endDate));
        }
        
        // Priority 2: Year and month
        if (year != null && month != null) {
            return ResponseEntity.ok(adminReportService.getDoctorPerformance(year, month));
        }
        
        // Priority 3: Default to current month
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(adminReportService.getDoctorPerformance(now.getYear(), now.getMonthValue()));
    }

    /**
     * Get invoices for the selected period
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceDto>> getInvoices(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer day,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return ResponseEntity.ok(adminReportService.getInvoicesByFilters(year, month, day, startDate, endDate));
    }
}
