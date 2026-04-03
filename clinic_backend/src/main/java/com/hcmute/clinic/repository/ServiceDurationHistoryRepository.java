package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.ServiceDurationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository quản lý lịch sử thời gian thực hiện dịch vụ (ServiceDurationHistory).
 * Dùng để thống kê và tối ưu hóa thời gian khám chữa bệnh.
 */
@Repository
public interface ServiceDurationHistoryRepository extends JpaRepository<ServiceDurationHistory, Long> {

    @Query("SELECT h FROM ServiceDurationHistory h WHERE h.service.id = :serviceId " +
           "AND h.completedAt >= :since ORDER BY h.completedAt DESC")
    List<ServiceDurationHistory> findByServiceIdAndCompletedAtAfter(
            @Param("serviceId") Long serviceId,
            @Param("since") LocalDateTime since);

    @Query("SELECT h FROM ServiceDurationHistory h WHERE h.service.id = :serviceId " +
           "AND h.doctor.id = :doctorId AND h.completedAt >= :since ORDER BY h.completedAt DESC")
    List<ServiceDurationHistory> findByServiceIdAndDoctorIdAndCompletedAtAfter(
            @Param("serviceId") Long serviceId,
            @Param("doctorId") Long doctorId,
            @Param("since") LocalDateTime since);
}
