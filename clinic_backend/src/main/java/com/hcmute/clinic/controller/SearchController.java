package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final PatientRepository patientRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    
    @GetMapping("/patients")
    public ResponseEntity<?> searchPatients(@RequestParam String keyword) {
        List<Patient> patients = patientRepository.searchPatients(keyword);
        List<Map<String, Object>> result = patients.stream()
            .map(p -> Map.of(
                "id", (Object) p.getId(),
                "name", p.getFirstName() + " " + p.getLastName(),
                "email", p.getEmail() != null ? p.getEmail() : "",
                "phone", p.getPhone() != null ? p.getPhone() : ""
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/services")
    public ResponseEntity<?> searchServices(@RequestParam String keyword) {
        List<Service> services = serviceRepository.searchServices(keyword);
        List<Map<String, Object>> result = services.stream()
            .map(s -> Map.of(
                "id", (Object) s.getId(),
                "name", s.getName(),
                "description", s.getDescription() != null ? s.getDescription() : "",
                "price", s.getPrice()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/appointments")
    public ResponseEntity<?> searchAppointmentsByDate(@RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<Appointment> appointments = appointmentRepository.findByDate(localDate);
            List<Map<String, Object>> result = appointments.stream()
                .map(a -> Map.of(
                    "id", (Object) a.getId(),
                    "patientName", a.getPatient().getFirstName() + " " + a.getPatient().getLastName(),
                    "doctorName", a.getDoctor() != null ? 
                        a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName() : "",
                    "serviceName", a.getService() != null ? a.getService().getName() : "",
                    "datetime", a.getAppointmentDatetime().toString(),
                    "status", a.getStatus().toString()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid date format"));
        }
    }
}
