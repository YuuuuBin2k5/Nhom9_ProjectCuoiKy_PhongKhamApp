package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.DoctorPatientMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorPatientMessageRepository extends JpaRepository<DoctorPatientMessage, Long> {

    List<DoctorPatientMessage> findByPatient_IdAndDoctor_IdOrderByCreatedAtAsc(Long patientId, Long doctorId);
}
