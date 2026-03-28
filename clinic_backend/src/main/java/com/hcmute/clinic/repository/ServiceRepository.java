package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Service;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByActiveTrueOrderByNameAsc();
    List<Service> findAllByOrderByNameAsc();
    
    // Search functionality
    @Query("SELECT s FROM Service s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Service> searchServices(@Param("keyword") String keyword);
}
