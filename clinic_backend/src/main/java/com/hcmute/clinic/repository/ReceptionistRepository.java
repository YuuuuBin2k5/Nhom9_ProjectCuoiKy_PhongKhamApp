package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Receptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {
    Optional<Receptionist> findByEmail(String email);
    boolean existsByEmail(String email);
}
