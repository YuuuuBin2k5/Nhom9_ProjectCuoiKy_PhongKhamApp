package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.ServiceCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository quản lý các danh mục dịch vụ nha khoa (ServiceCategory).
 */
@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    /**
     * Tìm kiếm danh mục dịch vụ theo tên.
     * 
     * @param name Tên của danh mục dịch vụ.
     * @return Một Optional chứa ServiceCategory nếu tìm thấy, ngược lại trả về Optional rỗng.
     */
    Optional<ServiceCategory> findByName(String name);
}
