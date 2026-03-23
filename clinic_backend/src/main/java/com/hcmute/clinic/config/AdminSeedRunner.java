package com.hcmute.clinic.config;

import com.hcmute.clinic.entity.Admin;
import com.hcmute.clinic.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class AdminSeedRunner implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminRepository.count() > 0) {
            return;
        }
        Admin admin = Admin.builder()
                .email("admin@gmail.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .firstName("Admin")
                .lastName("System")
                .isActive(true)
                .build();
        adminRepository.save(admin);
        log.info("Seeded admin@gmail.com / 123456");
    }
}
