package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.StepImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StepImageRepository extends JpaRepository<StepImage, Long> {
    List<StepImage> findByStepIdOrderByCreatedAtDesc(Long stepId);
}
