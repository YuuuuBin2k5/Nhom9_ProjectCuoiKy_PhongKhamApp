package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý dữ liệu thực thể Bệnh nhân (Patient).
 * Cung cấp chức năng tìm kiếm bệnh nhân nâng cao theo thông tin cá nhân.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByEmailIgnoreCase(String email);

    Optional<Patient> findByPhone(String phone);
    
    // Optimized: Fetch patient with profile in one query
    @EntityGraph(attributePaths = {"profile"})
    Optional<Patient> findById(Long id);
    
    // Optimized: Fetch patient with profile using JOIN FETCH
    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.profile WHERE p.id = :id")
    Optional<Patient> findByIdWithProfile(@Param("id") Long id);
    
    // Search functionality
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "p.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "p.email LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Patient> searchPatients(@Param("keyword") String keyword);
}
