package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.OtpChallenge;
import com.hcmute.clinic.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    Optional<OtpChallenge> findFirstByPhoneE164AndPurposeAndConsumedFalseOrderByIdDesc(
            String phoneE164, OtpPurpose purpose);

    Optional<OtpChallenge> findFirstByEmailAndPurposeAndConsumedFalseOrderByIdDesc(
            String email, OtpPurpose purpose);

    @Query("select count(o) > 0 from OtpChallenge o where o.phoneE164 = :phone and o.purpose = :purpose " +
            "and o.consumed = true and o.consumedAt >= :since")
    boolean existsVerifiedSince(
            @Param("phone") String phoneE164,
            @Param("purpose") OtpPurpose purpose,
            @Param("since") LocalDateTime since);
}
