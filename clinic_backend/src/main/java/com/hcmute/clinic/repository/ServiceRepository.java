package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository quản lý danh mục các dịch vụ nha khoa (Service).
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * Lấy danh sách các dịch vụ đang hoạt động, sắp xếp theo tên tăng dần.
     */
    List<Service> findByActiveTrueOrderByNameAsc();

    /**
     * Lấy tất cả dịch vụ, sắp xếp theo tên tăng dần.
     */
    List<Service> findAllByOrderByNameAsc();

    /**
     * Tìm kiếm dịch vụ theo tên.
     */
    Optional<Service> findByName(String name);
    
    // Optimized: Fetch service with category and images in one query
    @EntityGraph(attributePaths = {"category", "images"})
    Optional<Service> findById(Long id);
    
    // Optimized: Fetch service with images to avoid N+1
    @Query("SELECT DISTINCT s FROM Service s LEFT JOIN FETCH s.images LEFT JOIN FETCH s.category WHERE s.id = :id")
    Optional<Service> findByIdWithImages(@Param("id") Long id);
    
    // Search functionality
    @Query("SELECT s FROM Service s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Service> searchServices(@Param("keyword") String keyword);
    
    List<Service> findByCategoryId(Long categoryId);
}
