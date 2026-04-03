package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repository quản lý dữ liệu thực thể Bác sĩ (Doctor).
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    /**
     * Tìm kiếm bác sĩ theo địa chỉ email (không phân biệt hoa thường).
     * @param email Địa chỉ email của bác sĩ.
     * @return Một Optional chứa bác sĩ nếu tìm thấy.
     */
    Optional<Doctor> findByEmailIgnoreCase(String email);
    
    /**
     * Lấy danh sách tất cả bác sĩ theo phân trang, bao gồm thông tin phòng khám.
     * @param pageable Thông tin phân trang.
     * @return Trang chứa danh sách bác sĩ.
     */
    @EntityGraph(attributePaths = {"clinicRoom"})
    Page<Doctor> findAll(Pageable pageable);
}
