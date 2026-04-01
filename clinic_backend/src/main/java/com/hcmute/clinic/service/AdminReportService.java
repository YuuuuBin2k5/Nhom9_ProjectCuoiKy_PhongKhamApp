package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.enums.InvoiceStatus;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {
    
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final InvoiceRepository invoiceRepository;
    
    public RevenueReportDto getRevenueReport(int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        
        return calculateRevenueReport(startDate, endDate, year, month);
    }
    
    /**
     * Get revenue report by date range (NEW - for frontend compatibility)
     */
    public RevenueReportDto getRevenueReportByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return calculateRevenueReport(startDateTime, endDateTime, 
                                     startDate.getYear(), startDate.getMonthValue());
    }
    
    /**
     * Common logic for calculating revenue report
     */
    private RevenueReportDto calculateRevenueReport(LocalDateTime startDateTime, 
                                                     LocalDateTime endDateTime,
                                                     int year, int month) {
        List<Appointment> allAppointments = appointmentRepository
            .findByAppointmentDatetimeBetween(startDateTime, endDateTime);
        
        List<Appointment> completed = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
            .collect(Collectors.toList());
        
        List<Appointment> cancelled = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
            .collect(Collectors.toList());
        
        // Calculate revenue from invoices as requested by user
        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        BigDecimal totalRevenue = invoices.stream()
            .filter(i -> i.getPaymentStatus() != InvoiceStatus.CANCELLED)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgRevenue = completed.isEmpty() ? BigDecimal.ZERO :
            totalRevenue.divide(BigDecimal.valueOf(completed.size()), 2, RoundingMode.HALF_UP);
        
        return RevenueReportDto.builder()
            .year(year)
            .month(month)
            .totalRevenue(totalRevenue)
            .totalAppointments(allAppointments.size())
            .completedAppointments(completed.size())
            .cancelledAppointments(cancelled.size())
            .averageRevenuePerAppointment(avgRevenue)
            .build();
    }

    /**
     * Get invoices for the selected period
     */
    public List<InvoiceDto> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        return invoices.stream()
            .map(this::mapToInvoiceDto)
            .collect(Collectors.toList());
    }

    /**
     * Get invoices with flexible filters (Year, Month, Day OR StartDate, EndDate)
     */
    public List<InvoiceDto> getInvoicesByFilters(Integer year, Integer month, Integer day, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (startDate != null && endDate != null) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(23, 59, 59);
        } else if (year != null) {
            if (month != null && day != null) {
                // Specific day
                LocalDate date = LocalDate.of(year, month, day);
                startDateTime = date.atStartOfDay();
                endDateTime = date.atTime(23, 59, 59);
            } else if (month != null) {
                // Entire month
                LocalDate start = LocalDate.of(year, month, 1);
                startDateTime = start.atStartOfDay();
                endDateTime = start.withDayOfMonth(start.lengthOfMonth()).atTime(23, 59, 59);
            } else {
                // Entire year
                LocalDate start = LocalDate.of(year, 1, 1);
                startDateTime = start.atStartOfDay();
                endDateTime = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
            }
        } else {
            // Default to current month if no parameters provided
            LocalDate now = LocalDate.now();
            startDateTime = now.withDayOfMonth(1).atStartOfDay();
            endDateTime = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);
        }

        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        return invoices.stream()
            .map(this::mapToInvoiceDto)
            .collect(Collectors.toList());
    }

    private InvoiceDto mapToInvoiceDto(Invoice invoice) {
        Long treatmentPlanId = invoice.getTreatmentPlan() != null ? invoice.getTreatmentPlan().getId() : null;
        List<InvoiceDto.InvoiceItemDto> itemDtos = java.util.Collections.emptyList();
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            itemDtos = invoice.getItems().stream()
                .map(i -> InvoiceDto.InvoiceItemDto.builder()
                    .serviceName(i.getServiceName())
                    .toothNumber(i.getToothNumber())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .totalPrice(i.getTotalPrice())
                    .description(i.getDescription())
                    .build())
                .collect(Collectors.toList());
        }
        return InvoiceDto.builder()
            .id(invoice.getId())
            .patientId(invoice.getPatient().getId())
            .treatmentPlanId(treatmentPlanId)
            .patientName(invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName())
            .totalAmount(invoice.getTotalAmount())
            .paymentStatus(invoice.getPaymentStatus().toString())
            .paymentMethod(invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().toString() : null)
            .paidAt(invoice.getPaidAt())
            .createdAt(invoice.getCreatedAt())
            .items(itemDtos)
            .build();
    }
    
    public List<ServiceStatsDto> getTopServices(int year, int month, int limit) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        
        return calculateTopServices(startDate, endDate, limit);
    }
    
    /**
     * Get top services by date range (NEW - for frontend compatibility)
     */
    public List<ServiceStatsDto> getTopServicesByDateRange(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return calculateTopServices(startDateTime, endDateTime, limit);
    }
    
    /**
     * Common logic for calculating top services
     */
    private List<ServiceStatsDto> calculateTopServices(LocalDateTime startDateTime, 
                                                        LocalDateTime endDateTime, 
                                                        int limit) {
        List<Appointment> completed = appointmentRepository
            .findByStatusAndAppointmentDatetimeBetween(
                AppointmentStatus.COMPLETED, 
                startDateTime, 
                endDateTime
            );
        
        return completed.stream()
            .filter(a -> a.getService() != null)
            .collect(Collectors.groupingBy(Appointment::getService))
            .entrySet().stream()
            .map(entry -> {
                com.hcmute.clinic.entity.Service service = entry.getKey();
                List<Appointment> appointments = entry.getValue();
                
                BigDecimal revenue = appointments.stream()
                    .map(a -> service.getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                List<Review> reviews = reviewRepository.findByServiceIdOrderByCreatedAtDesc(service.getId());
                BigDecimal avgRating = reviews.isEmpty() ? BigDecimal.ZERO :
                    BigDecimal.valueOf(reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0));
                
                return ServiceStatsDto.builder()
                    .serviceId(service.getId())
                    .serviceName(service.getName())
                    .totalBookings(appointments.size())
                    .totalRevenue(revenue)
                    .averageRating(avgRating.setScale(1, RoundingMode.HALF_UP))
                    .totalReviews(reviews.size())
                    .build();
            })
            .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<DoctorStatsDto> getDoctorPerformance(int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        
        return calculateDoctorPerformance(startDate, endDate);
    }
    
    /**
     * Get doctor performance by date range (NEW - for frontend compatibility)
     */
    public List<DoctorStatsDto> getDoctorPerformanceByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return calculateDoctorPerformance(startDateTime, endDateTime);
    }
    
    /**
     * Common logic for calculating doctor performance
     */
    private List<DoctorStatsDto> calculateDoctorPerformance(LocalDateTime startDateTime, 
                                                             LocalDateTime endDateTime) {
        List<Appointment> allAppointments = appointmentRepository
            .findByAppointmentDatetimeBetween(startDateTime, endDateTime);
        
        return allAppointments.stream()
            .filter(a -> a.getDoctor() != null)
            .collect(Collectors.groupingBy(Appointment::getDoctor))
            .entrySet().stream()
            .map(entry -> {
                Doctor doctor = entry.getKey();
                List<Appointment> appointments = entry.getValue();
                
                List<Appointment> completed = appointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .collect(Collectors.toList());
                
                BigDecimal revenue = completed.stream()
                    .map(a -> a.getService() != null ? a.getService().getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                List<Review> reviews = reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctor.getId());
                BigDecimal avgRating = reviews.isEmpty() ? BigDecimal.ZERO :
                    BigDecimal.valueOf(reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0));
                
                return DoctorStatsDto.builder()
                    .doctorId(doctor.getId())
                    .doctorName(doctor.getFirstName() + " " + doctor.getLastName())
                    .specialization(doctor.getSpecialization())
                    .totalAppointments(appointments.size())
                    .completedAppointments(completed.size())
                    .totalRevenue(revenue)
                    .averageRating(avgRating.setScale(1, RoundingMode.HALF_UP))
                    .totalReviews(reviews.size())
                    .build();
            })
            .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
            .collect(Collectors.toList());
    }
}
