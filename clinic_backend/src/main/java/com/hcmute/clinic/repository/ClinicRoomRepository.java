package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.ClinicRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository quản lý thông tin các phòng khám/phòng chức năng (ClinicRoom).
 */
@Repository
public interface ClinicRoomRepository extends JpaRepository<ClinicRoom, Long> {
}
