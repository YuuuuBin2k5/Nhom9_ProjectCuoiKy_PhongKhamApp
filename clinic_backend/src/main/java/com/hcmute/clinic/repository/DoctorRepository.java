package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmailIgnoreCase(String email);
    
    @EntityGraph(attributePaths = {"clinicRoom"})
    Page<Doctor> findAll(Pageable pageable);
}
