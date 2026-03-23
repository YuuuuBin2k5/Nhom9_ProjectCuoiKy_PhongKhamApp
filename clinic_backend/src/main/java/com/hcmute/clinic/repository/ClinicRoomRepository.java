package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.ClinicRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicRoomRepository extends JpaRepository<ClinicRoom, Long> {
}
