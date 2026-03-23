package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.TreatmentPlan;
import com.hcmute.clinic.enums.TreatmentPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Long> {

    List<TreatmentPlan> findByPatientIdAndStatusOrderByCreatedAtDesc(Long patientId, TreatmentPlanStatus status);

    @Query("SELECT p FROM TreatmentPlan p LEFT JOIN FETCH p.steps s LEFT JOIN FETCH s.service LEFT JOIN FETCH s.clinicRoom WHERE p.patient.id = :patientId ORDER BY p.createdAt DESC")
    List<TreatmentPlan> findByPatientIdOrderByCreatedAtDesc(@Param("patientId") Long patientId);

    @Query("SELECT p FROM TreatmentPlan p LEFT JOIN FETCH p.steps s LEFT JOIN FETCH s.service LEFT JOIN FETCH s.clinicRoom WHERE p.id = :id")
    java.util.Optional<TreatmentPlan> findByIdWithSteps(@Param("id") Long id);
}
