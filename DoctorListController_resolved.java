package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.repository.AppointmentRepository;
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

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorListController {

    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping
    public ResponseEntity<?> listActive(@RequestParam(required = false) Long serviceId) {
        String filterName = null;
        if (serviceId != null) {
            Optional<Service> serviceOpt = serviceRepository.findById(serviceId);
            if (serviceOpt.isPresent()) {
                filterName = serviceOpt.get().getName();
            }
        }

        final String finalFilterName = filterName != null ? filterName.toLowerCase() : null;

        List<DoctorDto> dtos = doctorRepository.findAll().stream()
                .filter(d -> d.isActive())
                .filter(d -> {
                    if (finalFilterName == null) return true;
                    String spec = d.getSpecialization();
                    if (spec == null || spec.isEmpty()) return false;
                    String lspec = spec.toLowerCase();
                    
                    // Match if specialization is in service name OR service name is in specialization
                    return lspec.contains(finalFilterName) || finalFilterName.contains(lspec);
                })
                .map(d -> new DoctorDto(
                        d.getId(),
                        d.getFirstName() != null ? d.getFirstName() : "",
                        d.getLastName() != null ? d.getLastName() : "",
                        d.getSpecialization() != null ? d.getSpecialization() : "",
                        d.getClinicRoom() != null ? d.getClinicRoom().getName() : null,
                        d.getExperienceYears(),
                        (int) appointmentRepository.countByDoctorId(d.getId())
                ))
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
            int appointmentCount
    ) {}
}
