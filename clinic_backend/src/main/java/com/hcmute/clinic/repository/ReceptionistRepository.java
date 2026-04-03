package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Receptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository quản lý dữ liệu thực thể Lễ tân (Receptionist).
 */
@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {
    /**
     * Tìm kiếm lễ tân theo email.
     * @param email Email của lễ tân.
     * @return Optional chứa lễ tân nếu tìm thấy.
     */
    Optional<Receptionist> findByEmail(String email);

    /**
     * Tìm kiếm lễ tân theo email (không phân biệt hoa thường).
     * @param email Email của lễ tân.
     * @return Optional chứa lễ tân nếu tìm thấy.
     */
    Optional<Receptionist> findByEmailIgnoreCase(String email);

    /**
     * Kiểm tra xem email đã tồn tại trong hệ thống chưa.
     * @param email Email cần kiểm tra.
     * @return true nếu tồn tại, ngược lại false.
     */
    boolean existsByEmail(String email);
}
