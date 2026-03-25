package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorListController {

    private final DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<?> listActive() {
        List<DoctorDto> dtos = doctorRepository.findAll().stream()
                .filter(d -> d.isActive())
                .map(d -> new DoctorDto(
                        d.getId(),
                        d.getFirstName() != null ? d.getFirstName() : "",
                        d.getLastName() != null ? d.getLastName() : "",
                        d.getSpecialization() != null ? d.getSpecialization() : "",
                        d.getClinicRoom() != null ? d.getClinicRoom().getName() : null,
                        d.getExperienceYears(),
                        d.isActive()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public record DoctorDto(Long id, String firstName, String lastName, String specialization, String roomName, Integer experienceYears, boolean active) {}
}
