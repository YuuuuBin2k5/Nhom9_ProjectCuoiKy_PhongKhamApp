package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.TreatmentPlanStep;
import com.hcmute.clinic.enums.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanStepRepository extends JpaRepository<TreatmentPlanStep, Long> {
    
    // New methods for tooth service calculation
    List<TreatmentPlanStep> findByPlanId(Long planId);
    
    List<TreatmentPlanStep> findByPlanIdAndToothNumber(Long planId, String toothNumber);
    
    List<TreatmentPlanStep> findByPlanIdOrderBySequenceOrder(Long planId);
    
    List<TreatmentPlanStep> findByPlanIdAndIsGeneralService(Long planId, boolean isGeneralService);
    
    Optional<TreatmentPlanStep> findByPlanIdAndToothNumberAndServiceId(
        Long planId, String toothNumber, Long serviceId);


    /**
     * Find the first IN_PROGRESS X-Ray step linked to a given appointment.
     * Used by completeXRay() to mark the step COMPLETED when the patient returns.
     */
    @Query("""
        SELECT s FROM TreatmentPlanStep s
        JOIN s.plan p
        WHERE p.appointment.id = :appointmentId
          AND s.status = :status
          AND (LOWER(s.service.name) LIKE '%x-quang%'
            OR LOWER(s.service.name) LIKE '%xquang%'
            OR LOWER(s.service.name) LIKE '%x quang%'
            OR LOWER(s.service.name) LIKE '%panorama%'
            OR LOWER(s.service.name) LIKE '%x-ray%'
            OR LOWER(s.service.name) LIKE '%xray%')
        ORDER BY s.sequenceOrder ASC
        """)
    Optional<TreatmentPlanStep> findInProgressXRayStepByAppointmentId(
            @Param("appointmentId") Long appointmentId,
            @Param("status") StepStatus status);

    /**
     * Convenience overload defaulting to IN_PROGRESS status.
     */
    default Optional<TreatmentPlanStep> findInProgressXRayStepByAppointmentId(Long appointmentId) {
        return findInProgressXRayStepByAppointmentId(appointmentId, StepStatus.IN_PROGRESS);
    }
}
