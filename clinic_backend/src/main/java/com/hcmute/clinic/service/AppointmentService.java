package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.TimeSlotDto;
import com.hcmute.clinic.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Dịch vụ AppointmentService (Dịch vụ Lịch hẹn) - Cung cấp logic nghiệp vụ liên quan đến quản lý cuộc hẹn.
 * Tập trung vào việc kiểm tra sự sẵn sàng của bác sĩ và các khung giờ trống (Time Slots).
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    
    /**
     * Lấy danh sách các khung giờ (Time Slots) còn trống của bác sĩ trong một ngày.
     * @param doctorId ID của bác sĩ cần kiểm tra.
     * @param date Ngày cần lấy lịch.
     * @return Danh sách các khung giờ kèm trạng thái (trống/bận).
     */
    public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        List<TimeSlotDto> slots = new ArrayList<>();
        
        // Clinic hours: 08:00 - 16:40
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(16, 40);
        
        // Generate 30-minute slots
        LocalTime current = start;
        while (current.isBefore(end) || current.equals(end)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, current);
            
            // Check if slot is in the past
            boolean isPast = slotDateTime.isBefore(LocalDateTime.now());
            
            // Check if doctor is busy (within 30 minutes window)
            boolean isBusy = false;
            String reason = null;
            
            if (!isPast) {
                // (Note: Removed doctor availability check per user request to allow multiple bookings)
                isBusy = false; 
            } else {
                reason = "Đã qua giờ";
            }
            
            slots.add(TimeSlotDto.builder()
                .time(current.toString())
                .available(!isPast && !isBusy)
                .reason(reason)
                .build());
            
            current = current.plusMinutes(30);
        }
        
        return slots;
    }
}
