package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.TreatmentPlanTemplateStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository quản lý các bước định sẵn trong mẫu phác đồ (TreatmentPlanTemplateStep).
 */
@Repository
public interface TreatmentPlanTemplateStepRepository extends JpaRepository<TreatmentPlanTemplateStep, Long> {
    /**
     * Kiểm tra xem bước phác đồ có tồn tại dựa trên ID dịch vụ hay không.
     *
     * @param serviceId ID của dịch vụ cần kiểm tra.
     * @return true nếu tồn tại, ngược lại trả về false.
     */
    boolean existsByServiceId(Long serviceId);
}
