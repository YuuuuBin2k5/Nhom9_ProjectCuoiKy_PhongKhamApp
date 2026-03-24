package com.hcmute.clinic.config;

import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.repository.ClinicRoomRepository;
import com.hcmute.clinic.repository.DoctorRepository;
import com.hcmute.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class DoctorPatientSeedRunner implements ApplicationRunner {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Seed doctor if not exists
        if (doctorRepository.findByEmailIgnoreCase("doctor@gmail.com").isEmpty()) {
            ClinicRoom room = clinicRoomRepository.findAll().stream()
                    .findFirst()
                    .orElse(null);
            
            Doctor doctor = Doctor.builder()
                    .email("doctor@gmail.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .firstName("Nguyễn")
                    .lastName("Văn Bác")
                    .isActive(true)
                    .clinicRoom(room)
                    .specialization("Nha khoa tổng quát")
                    .licenseNumber("BS-001")
                    .experienceYears(10)
                    .build();
            doctorRepository.save(doctor);
            log.info("Seeded doctor@gmail.com / 123456");
        }

        // Seed patient if not exists
        if (patientRepository.findByEmailIgnoreCase("patient@gmail.com").isEmpty()) {
            Patient patient = Patient.builder()
                    .email("patient@gmail.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .firstName("Test")
                    .lastName("Patient")
                    .isActive(true)
                    .build();
            patient = patientRepository.save(patient);
            patient.setQrCodeData("patient:" + patient.getId());
            patientRepository.save(patient);
            log.info("Seeded patient@gmail.com / 123456");
        }
    }
}
