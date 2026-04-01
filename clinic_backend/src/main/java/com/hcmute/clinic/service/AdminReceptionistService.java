package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.Receptionist;
import com.hcmute.clinic.repository.ReceptionistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Receptionist> getAllReceptionists() {
        return receptionistRepository.findAll();
    }

    @Transactional
    public Receptionist createReceptionist(Map<String, String> req) {
        String email = req.getOrDefault("email", "").trim().toLowerCase();
        if (email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (receptionistRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Receptionist receptionist = Receptionist.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getOrDefault("password", "123456")))
                .firstName(req.getOrDefault("firstName", ""))
                .lastName(req.getOrDefault("lastName", ""))
                .phoneNumber(req.getOrDefault("phone", ""))
                .isActive(true)
                .build();

        return receptionistRepository.save(receptionist);
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receptionist not found"));
        receptionist.setIsActive(active);
        receptionistRepository.save(receptionist);
    }
    
    @Transactional
    public void delete(Long id) {
        receptionistRepository.deleteById(id);
    }
}
