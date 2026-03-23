package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.TreatmentPlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanTemplateRepository extends JpaRepository<TreatmentPlanTemplate, Long> {

    List<TreatmentPlanTemplate> findByActiveTrueOrderByNameAsc();

    @Query("SELECT t FROM TreatmentPlanTemplate t LEFT JOIN FETCH t.steps s LEFT JOIN FETCH s.service LEFT JOIN FETCH s.clinicRoom WHERE t.id = :id")
    Optional<TreatmentPlanTemplate> findByIdWithSteps(@Param("id") Long id);
}
