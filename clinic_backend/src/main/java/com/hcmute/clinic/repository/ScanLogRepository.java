package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.ScanLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {
    List<ScanLog> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after, Pageable pageable);
}
