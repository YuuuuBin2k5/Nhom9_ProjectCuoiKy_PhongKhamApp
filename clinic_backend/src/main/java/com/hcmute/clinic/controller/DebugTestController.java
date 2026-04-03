package com.hcmute.clinic.controller;

import com.hcmute.clinic.security.JwtService;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller phục vụ mục đích kiểm thử và phát triển (Debug).
 */
@RestController
@RequestMapping("/api/debug-test")
@RequiredArgsConstructor
public class DebugTestController {

    private final JwtService jwtService;
    private final PatientRepository patientRepository;

    /**
     * Lấy token xác thực cho bệnh nhân đầu tiên trong hệ thống.
     * @return Chuỗi JWT token.
     */
    @GetMapping("/token")
    public String getToken() {
        Patient p = patientRepository.findAll().stream().findFirst().orElseThrow();
        return jwtService.generateToken(String.valueOf(p.getId()), "PATIENT");
    }
}
