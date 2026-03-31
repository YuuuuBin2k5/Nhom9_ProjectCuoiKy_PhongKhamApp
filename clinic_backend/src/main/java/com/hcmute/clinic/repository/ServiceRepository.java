package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByActiveTrueOrderByNameAsc();
    List<Service> findAllByOrderByNameAsc();
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
