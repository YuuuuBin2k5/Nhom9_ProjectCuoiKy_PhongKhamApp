package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.TreatmentPlanStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentPlanStepRepository extends JpaRepository<TreatmentPlanStep, Long> {
}
