package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository quản lý các đánh giá và phản hồi của bệnh nhân (Review).
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Tìm danh sách đánh giá của một bệnh nhân, sắp xếp theo thời gian tạo giảm dần.
     */
    List<Review> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /**
     * Tìm danh sách đánh giá của một bác sĩ, sắp xếp theo thời gian tạo giảm dần.
     */
    List<Review> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    /**
     * Tìm danh sách đánh giá của một dịch vụ, sắp xếp theo thời gian tạo giảm dần.
     */
    List<Review> findByServiceIdOrderByCreatedAtDesc(Long serviceId);

    /**
     * Tìm danh sách đánh giá theo ID cuộc hẹn.
     */
    List<Review> findByAppointmentId(Long appointmentId);

    /**
     * Kiểm tra xem đánh giá cho cuộc hẹn đã tồn tại hay chưa.
     */
    boolean existsByAppointmentId(Long appointmentId);

    /**
     * Kiểm tra xem bác sĩ đã có đánh giá nào chưa.
     */
    boolean existsByDoctorId(Long doctorId);
}
