package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.CreateDoctorRequest;
import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDoctorService {

    private final DoctorRepository doctorRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ReviewRepository reviewRepository;
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

    @Transactional
    public void updateDoctorStatus(Long id, boolean active) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + id));
        doctor.setActive(active);
        doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Long id, CreateDoctorRequest req) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + id));

        // Update basic info
        if (req.getFirstName() != null) {
            doctor.setFirstName(req.getFirstName().trim());
        }
        if (req.getLastName() != null) {
            doctor.setLastName(req.getLastName().trim());
        }
        
        // Update email if changed
        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            String newEmail = req.getEmail().trim().toLowerCase();
            if (!newEmail.equals(doctor.getEmail())) {
                if (doctorRepository.findByEmailIgnoreCase(newEmail).isPresent()) {
                    throw new IllegalArgumentException("Email already registered");
                }
                doctor.setEmail(newEmail);
            }
        }
        
        // Update password if provided
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            if (req.getPassword().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            doctor.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        
        // Update specialization
        if (req.getSpecialty() != null) {
            doctor.setSpecialization(req.getSpecialty());
        } else if (req.getSpecialization() != null) {
            doctor.setSpecialization(req.getSpecialization());
        }
        
        // Update license number
        if (req.getLicenseNumber() != null) {
            doctor.setLicenseNumber(req.getLicenseNumber());
        }
        
        // Update experience years
        if (req.getExperienceYears() != null) {
            doctor.setExperienceYears(req.getExperienceYears());
        }
        
        // Update clinic room
        if (req.getClinicRoomId() != null) {
            ClinicRoom room = clinicRoomRepository.findById(req.getClinicRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));
            doctor.setClinicRoom(room);
        }

        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bác sĩ không tồn tại với id: " + id));

        // Check for associations
        if (appointmentRepository.existsByDoctorId(id)) {
            throw new IllegalArgumentException("Không thể xóa bác sĩ đã có lịch hẹn");
        }
        if (medicalRecordRepository.existsByDoctorId(id)) {
            throw new IllegalArgumentException("Không thể xóa bác sĩ đã có hồ sơ bệnh án");
        }
        if (prescriptionRepository.existsByDoctorId(id)) {
            throw new IllegalArgumentException("Không thể xóa bác sĩ đã có đơn thuốc");
        }
        if (reviewRepository.existsByDoctorId(id)) {
            throw new IllegalArgumentException("Không thể xóa bác sĩ đã có đánh giá từ khách hàng");
        }

        doctorRepository.delete(doctor);
    }
}
