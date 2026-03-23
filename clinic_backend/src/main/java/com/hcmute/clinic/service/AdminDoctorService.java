package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.CreateDoctorRequest;
import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.repository.ClinicRoomRepository;
import com.hcmute.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDoctorService {

    private final DoctorRepository doctorRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Doctor createDoctor(CreateDoctorRequest req) {
        String email = (req.getEmail() != null ? req.getEmail().trim() : "").toLowerCase();
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (doctorRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered for a doctor");
        }

        ClinicRoom room = null;
        if (req.getClinicRoomId() != null) {
            room = clinicRoomRepository.findById(req.getClinicRoomId()).orElse(null);
        }

        Doctor doctor = Doctor.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName() != null ? req.getFirstName().trim() : "")
                .lastName(req.getLastName() != null ? req.getLastName().trim() : "")
                .clinicRoom(room)
                .specialization(req.getSpecialty() != null ? req.getSpecialty() : req.getSpecialization())
                .licenseNumber(req.getLicenseNumber())
                .isActive(true)
                .build();

        return doctorRepository.save(doctor);
    }
}
