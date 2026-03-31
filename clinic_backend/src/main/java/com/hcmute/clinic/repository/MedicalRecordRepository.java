package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    // Step 1: Fetch medical records with basic relations (no multiple bags)
    @Query("""
            SELECT DISTINCT mr FROM MedicalRecord mr
            LEFT JOIN FETCH mr.appointment a
            LEFT JOIN FETCH mr.doctor d
            LEFT JOIN FETCH d.clinicRoom
            LEFT JOIN FETCH mr.prescription p
            WHERE mr.patient.id = :patientId
            ORDER BY mr.createdAt DESC
            """)
    java.util.List<MedicalRecord> findByPatientIdWithBasicRelations(@Param("patientId") Long patientId);
    
    // Step 2: Fetch medical record details separately
    @Query("""
            SELECT DISTINCT mr FROM MedicalRecord mr
            LEFT JOIN FETCH mr.details det
            LEFT JOIN FETCH det.service s
            LEFT JOIN FETCH s.category
            WHERE mr IN :records
            """)
    java.util.List<MedicalRecord> fetchDetails(@Param("records") java.util.List<MedicalRecord> records);
    
    // Step 3: Fetch prescription details separately
    @Query("""
            SELECT DISTINCT mr FROM MedicalRecord mr
            LEFT JOIN FETCH mr.prescription p
            LEFT JOIN FETCH p.details
            WHERE mr IN :records
            """)
    java.util.List<MedicalRecord> fetchPrescriptionDetails(@Param("records") java.util.List<MedicalRecord> records);
    
    // Simple method without optimization (fallback)
    java.util.List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    
    Page<MedicalRecord> findByPatientId(Long patientId, Pageable pageable);
    
    java.util.Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
    boolean existsByDoctorId(Long doctorId);
}
