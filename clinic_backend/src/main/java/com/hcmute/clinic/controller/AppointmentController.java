package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.AppointmentRequest;
import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.enums.BookingType;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.DoctorRepository;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final com.hcmute.clinic.service.AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request, Authentication auth) {
        try {
            log.info("Creating appointment: {}", request);

            // 1. Resolve Patient
            Long patientId = request.getPatientId();
            if (patientId == null && auth != null) {
                // Try getting from auth if not in request
                try {
                    patientId = Long.parseLong(auth.getName());
                } catch (NumberFormatException ignored) {}
            }

            if (patientId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Patient ID is required"));
            }

            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân với ID: " + request.getPatientId()));

            // 2. Resolve Service
            if (request.getServiceId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Service ID is required"));
            }
            Service service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ với ID: " + request.getServiceId()));

            // 3. Resolve Doctor
            Doctor doctor = null;
            if (request.getDoctorId() == null) {
                log.info("Doctor ID is null, attempting to auto-assign a General Practitioner");
                // Auto-assign: first active General Practitioner
                doctor = doctorRepository.findAll().stream()
                        .filter(d -> d.isActive() && d.getSpecialization() != null && 
                                (d.getSpecialization().equalsIgnoreCase("Khám tổng quát") || 
                                 d.getSpecialization().equalsIgnoreCase("Nha khoa tổng quát")))
                        .findFirst()
                        .orElse(null);
                
                if (doctor == null) {
                    // Fallback: any active doctor
                    doctor = doctorRepository.findAll().stream().filter(d -> d.isActive()).findFirst().orElse(null);
                }

                if (doctor == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Hệ thống hiện tại chưa có bác sĩ nào sẵn sàng cho dịch vụ này."));
                }
            } else {
                doctor = doctorRepository.findById(request.getDoctorId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));
            }

            // 4. Parse DateTime
            LocalDateTime appointmentTime;
            try {
                // The app may send ISO format: "2026-03-24T10:00:00" OR space format: "2026-03-24 10:00:00"
                String dt = request.getAppointmentDatetime();
                if (dt != null && dt.contains(" ") && !dt.contains("T")) {
                    dt = dt.replace(" ", "T");
                }
                appointmentTime = LocalDateTime.parse(dt);
            } catch (Exception e) {
                log.error("Failed to parse datetime: {}", request.getAppointmentDatetime(), e);
                return ResponseEntity.badRequest().body(Map.of("message", "Định dạng ngày giờ không hợp lệ: " + request.getAppointmentDatetime()));
            }

            // 5. Validate time range (08:00 - 16:40) TRƯỚC kiểm tra quá khứ — tránh báo "quá khứ" khi giờ thực chất ngoài giờ làm việc (vd. 07:00 hôm nay).
            java.time.LocalTime time = appointmentTime.toLocalTime();
            java.time.LocalTime start = java.time.LocalTime.of(8, 0);
            java.time.LocalTime end = java.time.LocalTime.of(16, 40);
            if (time.isBefore(start) || time.isAfter(end)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thời gian đặt lịch phải từ 08:00 đến 16:40"));
            }

            // 6. Validate not in the past (sau khi đã nằm trong khung giờ)
            if (appointmentTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "Không thể đặt lịch trong quá khứ. Vui lòng chọn giờ sau thời điểm hiện tại, trong khung 08:00–16:40."));
            }

            // 6.1 Validate existing active appointments for patient
            boolean hasActiveAppt = appointmentRepository.existsByPatientIdAndStatusIn(patient.getId(), 
                java.util.List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.IN_PROGRESS));
            if (hasActiveAppt) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bệnh nhân đang có một lịch khám chưa hoàn thành. Không thể đặt thêm."));
            }

            // 5.2 Validate doctor availability (assume each appointment is ~30 mins)
            boolean doctorBusy = appointmentRepository.existsByDoctorIdAndAppointmentDatetimeBetween(
                doctor.getId(), 
                appointmentTime.minusMinutes(29), 
                appointmentTime.plusMinutes(29)
            );
            
            if (doctorBusy) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bác sĩ đã có lịch hẹn trong khung giờ này. Vui lòng chọn giờ khác."));
            }

            // 6. Create Appointment
            BookingType bookingType = BookingType.ONLINE;
            if (request.getBookingType() != null) {
                try {
                    bookingType = BookingType.valueOf(request.getBookingType());
                } catch (IllegalArgumentException ignored) {}
            }

            AppointmentStatus status = AppointmentStatus.SCHEDULED;
            if (request.getStatus() != null) {
                try {
                    status = AppointmentStatus.valueOf(request.getStatus());
                } catch (IllegalArgumentException ignored) {}
            }

            Appointment appointment = Appointment.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .service(service)
                    .appointmentDatetime(appointmentTime)
                    .bookingType(bookingType)
                    .status(status)
                    .build();

            Appointment saved = appointmentRepository.save(appointment);

            // 6. Return response (Matching UpcomingAppointment model in Android app)
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "datetime", saved.getAppointmentDatetime().toString(),
                    "serviceName", saved.getService().getName(),
                    "doctorName", (saved.getDoctor().getLastName() + " " + saved.getDoctor().getFirstName()).trim(),
                    "status", saved.getStatus().name()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating appointment", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }
    
    @GetMapping("/available-slots")
    public ResponseEntity<?> getAvailableSlots(
        @RequestParam Long doctorId,
        @RequestParam String date
    ) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            List<com.hcmute.clinic.dto.TimeSlotDto> slots = appointmentService.getAvailableSlots(doctorId, localDate);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            log.error("Error getting available slots", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
        @PathVariable Long id,
        @RequestBody(required = false) com.hcmute.clinic.dto.CancelRequest request,
        Authentication auth
    ) {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
            
            // Check ownership (patient or admin)
            if (auth != null && !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                try {
                    Long patientId = Long.parseLong(auth.getName());
                    if (!appointment.getPatient().getId().equals(patientId)) {
                        return ResponseEntity.status(403).body(Map.of("message", "Không có quyền hủy lịch hẹn này"));
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(403).body(Map.of("message", "Không có quyền hủy lịch hẹn này"));
                }
            }
            
            // Check if can cancel (at least 2 hours before)
            if (appointment.getAppointmentDatetime().isBefore(LocalDateTime.now().plusHours(2))) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Không thể hủy lịch hẹn trong vòng 2 giờ trước giờ khám"
                ));
            }
            
            // Check if already cancelled or completed
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                return ResponseEntity.badRequest().body(Map.of("message", "Lịch hẹn đã được hủy trước đó"));
            }
            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không thể hủy lịch hẹn đã hoàn thành"));
            }
            
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            
            return ResponseEntity.ok(Map.of(
                "message", "Hủy lịch hẹn thành công",
                "appointmentId", id
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error cancelling appointment", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
        @PathVariable Long id,
        @RequestBody com.hcmute.clinic.dto.RescheduleRequest request,
        Authentication auth
    ) {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));
            
            // Check ownership
            if (auth != null && !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                try {
                    Long patientId = Long.parseLong(auth.getName());
                    if (!appointment.getPatient().getId().equals(patientId)) {
                        return ResponseEntity.status(403).body(Map.of("message", "Không có quyền đổi lịch hẹn này"));
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(403).body(Map.of("message", "Không có quyền đổi lịch hẹn này"));
                }
            }
            
            // Validate new datetime
            LocalDateTime newDatetime = request.getNewDatetime();
            java.time.LocalTime time = newDatetime.toLocalTime();
            java.time.LocalTime start = java.time.LocalTime.of(8, 0);
            java.time.LocalTime end = java.time.LocalTime.of(16, 40);
            if (time.isBefore(start) || time.isAfter(end)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thời gian đặt lịch phải từ 08:00 đến 16:40"));
            }
            if (newDatetime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "Không thể đặt lịch trong quá khứ. Vui lòng chọn giờ sau thời điểm hiện tại, trong khung 08:00–16:40."));
            }
            
            // Check doctor availability
            boolean doctorBusy = appointmentRepository.existsByDoctorIdAndAppointmentDatetimeBetween(
                appointment.getDoctor().getId(),
                newDatetime.minusMinutes(29),
                newDatetime.plusMinutes(29)
            );
            if (doctorBusy) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Bác sĩ đã có lịch hẹn trong khung giờ này. Vui lòng chọn giờ khác."
                ));
            }
            
            appointment.setAppointmentDatetime(newDatetime);
            appointmentRepository.save(appointment);
            
            return ResponseEntity.ok(Map.of(
                "message", "Đổi lịch hẹn thành công",
                "appointmentId", id,
                "newDatetime", newDatetime.toString()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error rescheduling appointment", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}/date/{date}")
    public ResponseEntity<?> getAppointmentsByDoctorAndDate(
            @PathVariable Long doctorId,
            @PathVariable String date
    ) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            java.time.LocalDateTime start = localDate.atStartOfDay();
            java.time.LocalDateTime end = localDate.plusDays(1).atStartOfDay();
            
            log.info("Getting appointments for doctor {} on date {} (from {} to {})", 
                doctorId, date, start, end);
            
            List<Appointment> appts = appointmentRepository.findByDoctorIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                    doctorId, start, end);
            
            log.info("Found {} appointments for doctor {} on {}", appts.size(), doctorId, date);
            
            return ResponseEntity.ok(appts.stream().map(a -> {
                log.info("  - Appointment {}: patient={}, service={}, time={}, status={}", 
                    a.getId(), 
                    a.getPatient().getLastName() + " " + a.getPatient().getFirstName(),
                    a.getService().getName(),
                    a.getAppointmentDatetime(),
                    a.getStatus());
                
                return Map.of(
                    "id", a.getId(),
                    "patientName", (a.getPatient().getLastName() + " " + a.getPatient().getFirstName()).trim(),
                    "patientPhone", a.getPatient().getPhone() != null ? a.getPatient().getPhone() : "",
                    "serviceName", a.getService().getName(),
                    "datetime", a.getAppointmentDatetime().toString(),
                    "status", a.getStatus().name()
                );
            }).toList());
        } catch (Exception e) {
            log.error("Error getting doctor appointments", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }
}
