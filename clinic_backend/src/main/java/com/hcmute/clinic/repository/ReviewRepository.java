package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Review> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<Review> findByServiceIdOrderByCreatedAtDesc(Long serviceId);
    List<Review> findByAppointmentId(Long appointmentId);
    boolean existsByAppointmentId(Long appointmentId);
}
