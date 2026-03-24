package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    java.util.List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
