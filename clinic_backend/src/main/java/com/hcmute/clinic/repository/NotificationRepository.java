package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý các thông báo hệ thống (Notification).
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Tìm danh sách thông báo của bệnh nhân theo thứ tự thời gian giảm dần với phân trang.
     */
    List<Notification> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    /**
     * Tìm danh sách thông báo của bệnh nhân theo thứ tự thời gian giảm dần.
     */
    List<Notification> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /**
     * Tìm danh sách thông báo của bệnh nhân theo trạng thái đã đọc/chưa đọc, sắp xếp theo thời gian giảm dần.
     */
    List<Notification> findByPatientIdAndIsReadOrderByCreatedAtDesc(Long patientId, boolean isRead);

    /**
     * Kiểm tra sự tồn tại của thông báo dựa trên loại thông báo.
     */
    boolean existsByType(String type);
}
