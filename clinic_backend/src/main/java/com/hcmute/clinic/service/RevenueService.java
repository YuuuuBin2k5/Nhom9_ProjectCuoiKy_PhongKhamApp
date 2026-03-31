package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.RevenueCategoryDTO;
import com.hcmute.clinic.dto.RevenueReportDTO;
import com.hcmute.clinic.repository.InvoiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final InvoiceItemRepository invoiceItemRepository;

    public RevenueReportDTO getRevenueReport(Integer year, Integer month, Integer day) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        if (year == null) {
            year = LocalDateTime.now().getYear();
        }

        if (month != null && month >= 1 && month <= 12) {
            if (day != null && day >= 1 && day <= 31) {
                // Specific day
                startDate = LocalDateTime.of(year, month, day, 0, 0);
                endDate = LocalDateTime.of(year, month, day, 23, 59, 59);
            } else {
                // Full month
                startDate = LocalDateTime.of(year, month, 1, 0, 0);
                // End of month is tricky, simple way:
                endDate = startDate.plusMonths(1).minusSeconds(1);
            }
        } else {
            // Full year
            startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        }

        List<RevenueCategoryDTO> categories = invoiceItemRepository.calculateRevenueByCategory(startDate, endDate);
        
        BigDecimal total = categories.stream()
                .map(RevenueCategoryDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueReportDTO.builder()
                .totalRevenue(total)
                .categories(categories)
                .build();
    }
}
