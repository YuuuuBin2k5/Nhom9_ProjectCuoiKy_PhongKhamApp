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
                log.info("Doctor ID is null, attempting to auto-assign based on service category: {}", service.getCategory().getName());
                // Simple auto-assign: first active doctor with matching specialization
                doctor = doctorRepository.findAll().stream()
                        .filter(d -> d.isActive() && d.getSpecialization() != null && 
                                d.getSpecialization().toLowerCase().contains(service.getCategory().getName().toLowerCase()))
                        .findFirst()
                        .orElse(null);
                
                if (doctor == null) {
                    // Fallback: any active doctor if no specialty match found (though unlikely with good seeding)
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

            // 5. Validate Time Range (08:00 - 16:40)
            java.time.LocalTime time = appointmentTime.toLocalTime();
            java.time.LocalTime start = java.time.LocalTime.of(8, 0);
            java.time.LocalTime end = java.time.LocalTime.of(16, 40);
            
            if (time.isBefore(start) || time.isAfter(end)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thời gian đặt lịch phải từ 08:00 đến 16:40"));
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
}
