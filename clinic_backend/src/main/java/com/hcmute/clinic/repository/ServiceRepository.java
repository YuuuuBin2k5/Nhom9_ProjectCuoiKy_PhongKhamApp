package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Service;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByActiveTrueOrderByNameAsc();
    List<Service> findAllByOrderByNameAsc();
}
