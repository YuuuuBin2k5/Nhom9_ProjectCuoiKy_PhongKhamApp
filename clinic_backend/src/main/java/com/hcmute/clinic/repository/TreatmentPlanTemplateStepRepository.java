package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.TreatmentPlanTemplateStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentPlanTemplateStepRepository extends JpaRepository<TreatmentPlanTemplateStep, Long> {
    boolean existsByServiceId(Long serviceId);
}
