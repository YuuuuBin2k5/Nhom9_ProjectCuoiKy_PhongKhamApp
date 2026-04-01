package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.OtpChallenge;
import com.hcmute.clinic.enums.OtpPurpose;
import com.hcmute.clinic.repository.OtpChallengeRepository;
import com.hcmute.clinic.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpChallengeRepository otpChallengeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.ttl-minutes:5}")
    private int ttlMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.register-grace-minutes:15}")
    private int registerGraceMinutes;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void requestOtp(String rawPhone, OtpPurpose purpose) {
        String phone = PhoneUtils.normalizeVietnam(rawPhone);
        if (phone.length() < 10) {
            throw new IllegalArgumentException("Invalid phone");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        String hash = passwordEncoder.encode(code);

        OtpChallenge challenge = OtpChallenge.builder()
                .phoneE164(phone)
                .purpose(purpose)
                .codeHash(hash)
                .expiresAt(LocalDateTime.now().plusMinutes(ttlMinutes))
                .attempts(0)
                .consumed(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpChallengeRepository.save(challenge);

        log.info("[DEV] OTP for {} ({}) = {}", phone, purpose, code);
    }

    @Transactional
    public void requestOtpByEmail(String email, OtpPurpose purpose) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        String hash = passwordEncoder.encode(code);

        OtpChallenge challenge = OtpChallenge.builder()
                .email(email)
                .purpose(purpose)
                .codeHash(hash)
                .expiresAt(LocalDateTime.now().plusMinutes(ttlMinutes))
                .attempts(0)
                .consumed(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpChallengeRepository.save(challenge);

        log.info("[DEV] OTP for {} ({}) = {}", email, purpose, code);
    }

    @Transactional
    public boolean verifyAndConsume(String rawPhone, String code, OtpPurpose purpose) {
        String phone = PhoneUtils.normalizeVietnam(rawPhone);
        OtpChallenge challenge = otpChallengeRepository
                .findFirstByPhoneE164AndPurposeAndConsumedFalseOrderByIdDesc(phone, purpose)
                .orElse(null);
        if (challenge == null) {
            return false;
        }
        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (challenge.getAttempts() >= maxAttempts) {
            return false;
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            otpChallengeRepository.save(challenge);
            return false;
        }
        challenge.setConsumed(true);
        challenge.setConsumedAt(LocalDateTime.now());
        otpChallengeRepository.save(challenge);
        return true;
    }

    @Transactional
    public boolean verifyAndConsumeByEmail(String email, String code, OtpPurpose purpose) {
        OtpChallenge challenge = otpChallengeRepository
                .findFirstByEmailAndPurposeAndConsumedFalseOrderByIdDesc(email, purpose)
                .orElse(null);
        if (challenge == null) {
            return false;
        }
        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (challenge.getAttempts() >= maxAttempts) {
            return false;
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            otpChallengeRepository.save(challenge);
            return false;
        }
        challenge.setConsumed(true);
        challenge.setConsumedAt(LocalDateTime.now());
        otpChallengeRepository.save(challenge);
        return true;
    }

    public boolean hasRecentRegisterVerification(String rawPhone) {
        String phone = PhoneUtils.normalizeVietnam(rawPhone);
        LocalDateTime since = LocalDateTime.now().minusMinutes(registerGraceMinutes);
        return otpChallengeRepository.existsVerifiedSince(phone, OtpPurpose.REGISTER, since);
    }
}
