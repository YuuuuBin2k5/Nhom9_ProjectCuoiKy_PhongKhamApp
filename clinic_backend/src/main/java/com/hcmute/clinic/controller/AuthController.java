package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.entity.Admin;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.enums.OtpPurpose;
import com.hcmute.clinic.repository.AdminRepository;
import com.hcmute.clinic.repository.DoctorRepository;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.security.JwtService;
import com.hcmute.clinic.service.OtpService;
import com.hcmute.clinic.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        
        String email = request.getEmail().trim().toLowerCase();
        
        // Check admin first
        var admin = adminRepository.findByEmailIgnoreCase(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            if (a.isActive() && passwordEncoder.matches(request.getPassword(), a.getPasswordHash())) {
                return ResponseEntity.ok(AuthResponse.builder()
                        .token(jwtService.generateToken(String.valueOf(a.getId()), "ADMIN"))
                        .email(a.getEmail())
                        .role("ADMIN")
                        .build());
            }
        }
        
        // Check doctor
        var doctor = doctorRepository.findByEmailIgnoreCase(email);
        if (doctor.isPresent()) {
            Doctor d = doctor.get();
            if (d.isActive() && passwordEncoder.matches(request.getPassword(), d.getPasswordHash())) {
                return ResponseEntity.ok(AuthResponse.builder()
                        .token(jwtService.generateToken(String.valueOf(d.getId()), "DOCTOR"))
                        .email(d.getEmail())
                        .role("DOCTOR")
                        .build());
            }
        }
        
        // Check patient
        return patientRepository.findByEmailIgnoreCase(email)
                .filter(Patient::isActive)
                .filter(p -> {
                    String hash = p.getPasswordHash();
                    if (hash == null || hash.isBlank()) {
                        return false;
                    }
                    try {
                        return passwordEncoder.matches(request.getPassword(), hash);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(AuthResponse.builder()
                        .token(jwtService.generateToken(String.valueOf(p.getId()), "PATIENT"))
                        .email(p.getEmail())
                        .role("PATIENT")
                        .build()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password")));
    }

    @PostMapping("/otp/request")
    public ResponseEntity<?> requestOtp(@RequestBody OtpRequestDto body) {
        try {
            OtpPurpose purpose = OtpPurpose.valueOf(body.getPurpose().trim().toUpperCase());
            otpService.requestOtp(body.getPhone(), purpose);
            return ResponseEntity.ok(Map.of("message", "OTP sent"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid phone or purpose"));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(@RequestBody OtpVerifyDto body) {
        OtpPurpose purpose;
        try {
            purpose = OtpPurpose.valueOf(body.getPurpose().trim().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        String phone = PhoneUtils.normalizeVietnam(body.getPhone());
        if (!otpService.verifyAndConsume(phone, body.getCode(), purpose)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (purpose == OtpPurpose.REGISTER) {
            return ResponseEntity.ok(OtpVerifyResponse.builder()
                    .needsRegistration(true)
                    .build());
        }

        // LOGIN
        return patientRepository.findByPhone(phone)
                .map(p -> OtpVerifyResponse.builder()
                        .token(jwtService.generateToken(String.valueOf(p.getId()), "PATIENT"))
                        .email(p.getEmail())
                        .role("PATIENT")
                        .needsRegistration(false)
                        .build())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(OtpVerifyResponse.builder()
                        .needsRegistration(true)
                        .build()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
        }
        if (request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        }
        if (patientRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }
        if (request.getFirstName() == null || request.getFirstName().isBlank()
                || request.getLastName() == null || request.getLastName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "First name and last name are required"));
        }

        Patient patient = new Patient();
        patient.setEmail(email);
        patient.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        patient.setFirstName(request.getFirstName().trim());
        patient.setLastName(request.getLastName().trim());
        patient.setActive(true);

        patientRepository.save(patient);
        patient.setQrCodeData("patient:" + patient.getId());
        patientRepository.save(patient);

        String token = jwtService.generateToken(String.valueOf(patient.getId()), "PATIENT");
        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "token", token,
                "email", patient.getEmail(),
                "role", "PATIENT"
        ));
    }

    @PostMapping("/staff/login")
    public ResponseEntity<?> staffLogin(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        String email = request.getEmail().trim().toLowerCase();

        var admin = adminRepository.findByEmailIgnoreCase(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            if (!a.isActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Account inactive"));
            }
            if (passwordEncoder.matches(request.getPassword(), a.getPasswordHash())) {
                return ResponseEntity.ok(AuthResponse.builder()
                        .token(jwtService.generateToken(String.valueOf(a.getId()), "ADMIN"))
                        .email(a.getEmail())
                        .role("ADMIN")
                        .build());
            }
        }

        var doctor = doctorRepository.findByEmailIgnoreCase(email);
        if (doctor.isPresent()) {
            Doctor d = doctor.get();
            if (!d.isActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Account inactive"));
            }
            if (passwordEncoder.matches(request.getPassword(), d.getPasswordHash())) {
                return ResponseEntity.ok(AuthResponse.builder()
                        .token(jwtService.generateToken(String.valueOf(d.getId()), "DOCTOR"))
                        .email(d.getEmail())
                        .role("DOCTOR")
                        .build());
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
    }
}
