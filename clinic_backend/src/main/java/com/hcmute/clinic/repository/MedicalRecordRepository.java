package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    java.util.List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Page<MedicalRecord> findByPatientId(Long patientId, Pageable pageable);
    java.util.Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
    boolean existsByDoctorId(Long doctorId);
}
