package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.StepImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository quản lý hình ảnh minh chứng cho các bước điều trị.
 */
@Repository
public interface StepImageRepository extends JpaRepository<StepImage, Long> {
    /**
     * Tìm danh sách hình ảnh theo ID của bước điều trị, sắp xếp theo thời gian tạo giảm dần.
     * 
     * @param stepId ID của bước điều trị.
     * @return Danh sách các hình ảnh liên quan.
     */
    List<StepImage> findByStepIdOrderByCreatedAtDesc(Long stepId);
}
