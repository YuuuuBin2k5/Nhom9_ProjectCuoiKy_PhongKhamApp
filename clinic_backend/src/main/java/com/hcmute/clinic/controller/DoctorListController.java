package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.repository.DoctorRepository;
import com.hcmute.clinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorListController {

    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;

    @GetMapping
    public ResponseEntity<?> listActive(@RequestParam(required = false) Long serviceId) {
        String filterSpec = null;
        if (serviceId != null) {
            Optional<Service> svc = serviceRepository.findById(serviceId);
            if (svc.isPresent()) {
                filterSpec = svc.get().getCategory().getName();
            }
        }

        final String finalFilterSpec = filterSpec;
        List<Doctor> allActive = doctorRepository.findAll().stream()
                .filter(d -> d.isActive())
                .collect(Collectors.toList());

        List<DoctorDto> dtos = allActive.stream()
                .map(d -> {
                    boolean isSpecialist = false;
                    if (finalFilterSpec != null && d.getSpecialization() != null) {
                        isSpecialist = d.getSpecialization().toLowerCase().contains(finalFilterSpec.toLowerCase());
                    }
                    return new DoctorDto(
                        d.getId(),
                        d.getFirstName() != null ? d.getFirstName() : "",
                        d.getLastName() != null ? d.getLastName() : "",
                        d.getSpecialization() != null ? d.getSpecialization() : "",
                        d.getClinicRoom() != null ? d.getClinicRoom().getName() : null,
                        d.getExperienceYears(),
                        isSpecialist
                    );
                })
                .sorted((a, b) -> {
                    // Specialists first
                    if (a.isSpecialist && !b.isSpecialist) return -1;
                    if (!a.isSpecialist && b.isSpecialist) return 1;
                    return 0;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public record DoctorDto(
            Long id, 
            String firstName, 
            String lastName, 
            String specialization, 
            String roomName, 
            Integer experienceYears,
            boolean isSpecialist
    ) {}
}
