package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.ServiceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceImageRepository extends JpaRepository<ServiceImage, Long> {
}
