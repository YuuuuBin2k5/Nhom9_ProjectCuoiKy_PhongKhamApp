package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository quản lý dữ liệu thực thể Đơn thuốc (Prescription).
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    /**
     * Kiểm tra xem có đơn thuốc nào được tạo bởi bác sĩ có ID cho trước hay không.
     *
     * @param doctorId ID của bác sĩ cần kiểm tra.
     * @return true nếu tồn tại đơn thuốc, ngược lại trả về false.
     */
    boolean existsByDoctorId(Long doctorId);
}
