package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.DoctorDetailDto;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.DoctorRepository;
import com.hcmute.clinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDetailDto> getById(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .filter(Doctor::isActive)
                .map(d -> {
                    long apptCount = appointmentRepository.countByDoctorId(d.getId());
                    String bio = d.getBiography();
                    if (bio == null || bio.isBlank()) {
                        String spec = d.getSpecialization() != null ? d.getSpecialization() : "răng hàm mặt";
                        bio = "Bác sĩ có nhiều kinh nghiệm trong lĩnh vực " + spec
                                + ". Đồng hành cùng bệnh nhân với phong cách tận tình, chu đáo.";
                    }
                    return DoctorDetailDto.builder()
                            .id(d.getId())
                            .firstName(d.getFirstName())
                            .lastName(d.getLastName())
                            .specialization(d.getSpecialization())
                            .roomName(d.getClinicRoom() != null ? d.getClinicRoom().getName() : null)
                            .experienceYears(d.getExperienceYears())
                            .biography(bio)
                            .avatarUrl(d.getAvatarUrl())
                            .appointmentCount(apptCount)
                            .build();
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> listActive(@RequestParam(required = false) Long serviceId) {
        String filterName = null;
        if (serviceId != null) {
            Optional<Service> serviceOpt = serviceRepository.findById(serviceId);
            if (serviceOpt.isPresent() && serviceOpt.get().getCategory() != null) {
                filterName = serviceOpt.get().getCategory().getName();
            }
        }

        final String finalFilterName = filterName != null ? filterName.toLowerCase() : null;

        List<DoctorDto> dtos = doctorRepository.findAll().stream()
                .filter(d -> d.isActive())
                .filter(d -> {
                    // Loại trừ các bác sĩ thuộc phòng chuyên biệt/kỹ thuật khi liệt kê danh sách chung
                    if (finalFilterName == null && d.getClinicRoom() != null) {
                        String roomName = d.getClinicRoom().getName().toLowerCase();
                        if (roomName.contains("x-quang") || 
                            roomName.contains("xét nghiệm") || 
                            roomName.contains("siêu âm") || 
                            roomName.contains("chẩn đoán hình ảnh")) {
                            return false;
                        }
                    }
                    
                    if (finalFilterName == null) return true;
                    String spec = d.getSpecialization();
                    if (spec == null || spec.isEmpty()) return false;
                    String lspec = spec.toLowerCase().trim();
                    
                    return lspec.contains(finalFilterName.trim());
                })
                .map(d -> new DoctorDto(
                        d.getId(),
                        d.getFirstName() != null ? d.getFirstName() : "",
                        d.getLastName() != null ? d.getLastName() : "",
                        d.getSpecialization() != null ? d.getSpecialization() : "",
                        d.getClinicRoom() != null ? d.getClinicRoom().getName() : null,
                        d.getExperienceYears(),
                        d.isActive(),
                        d.getAvatarUrl()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public record DoctorDto(Long id, String firstName, String lastName, String specialization, String roomName, Integer experienceYears, boolean active, String avatarUrl) {}
}
