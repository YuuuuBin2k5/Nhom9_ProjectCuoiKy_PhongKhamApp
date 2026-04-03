package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý dữ liệu thực thể Lịch hẹn (Appointment).
 * Cung cấp các phương thức truy vấn lịch khám theo bác sĩ, bệnh nhân và thời gian (UC_02, UC_05).
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // Optimized: Fetch with patient and doctor to avoid N+1
    @Query("""
            SELECT a FROM Appointment a
            LEFT JOIN FETCH a.patient p
            LEFT JOIN FETCH p.profile
            LEFT JOIN FETCH a.doctor d
            LEFT JOIN FETCH d.clinicRoom
            LEFT JOIN FETCH a.service s
            LEFT JOIN FETCH s.category
            WHERE a.patient.id = :patientId
            AND a.appointmentDatetime BETWEEN :start AND :end
            ORDER BY a.appointmentDatetime ASC
            """)
    List<Appointment> findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
            @Param("patientId") Long patientId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    boolean existsByDoctorIdAndAppointmentDatetimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    boolean existsByDoctorIdAndAppointmentDatetimeBetweenAndIdNot(Long doctorId, LocalDateTime start, LocalDateTime end, Long id);


    boolean existsByPatientIdAndStatusIn(Long patientId, List<AppointmentStatus> statuses);

    default List<Appointment> findTodayByPatientId(Long patientId) {
        LocalDate today = LocalDate.now();
        return findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                patientId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
    }
    
    // Optimized: Fetch with patient and service to avoid N+1
    @Query("""
            SELECT a FROM Appointment a
            LEFT JOIN FETCH a.patient p
            LEFT JOIN FETCH p.profile
            LEFT JOIN FETCH a.doctor d
            LEFT JOIN FETCH d.clinicRoom
            LEFT JOIN FETCH a.service s
            LEFT JOIN FETCH s.category
            WHERE a.doctor.id = :doctorId
            AND a.appointmentDatetime BETWEEN :start AND :end
            ORDER BY a.appointmentDatetime ASC
            """)
    List<Appointment> findByDoctorIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
            @Param("doctorId") Long doctorId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    default List<Appointment> findTodayByDoctorId(Long doctorId) {
        LocalDate today = LocalDate.now();
        return findByDoctorIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                doctorId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
    }

    default List<Appointment> findUpcomingByPatientId(Long patientId, int daysAhead) {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = LocalDate.now().plusDays(daysAhead).atTime(23, 59, 59);
        return findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                patientId, from, to);
    }

    Optional<Appointment> findFirstByPatientIdAndStatusInOrderByAppointmentDatetimeDesc(
            Long patientId, List<AppointmentStatus> statuses);

    long countByDoctorId(Long doctorId);
    
    boolean existsByServiceId(Long serviceId);
    
    boolean existsByDoctorId(Long doctorId);
    
    List<Appointment> findByAppointmentDatetimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Appointment> findByStatusAndAppointmentDatetimeBetween(
        AppointmentStatus status, LocalDateTime start, LocalDateTime end);
    
    // Search by date
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDatetime) = :date")
    List<Appointment> findByDate(@Param("date") LocalDate date);
    
    // Optimized: Fetch with patient, doctor, service for patient history
    @Query("""
            SELECT a FROM Appointment a
            LEFT JOIN FETCH a.patient p
            LEFT JOIN FETCH p.profile
            LEFT JOIN FETCH a.doctor d
            LEFT JOIN FETCH d.clinicRoom
            LEFT JOIN FETCH a.service s
            LEFT JOIN FETCH s.category
            WHERE a.patient.id = :patientId
            ORDER BY a.appointmentDatetime DESC
            """)
    List<Appointment> findByPatientIdOrderByAppointmentDatetimeDesc(@Param("patientId") Long patientId);
}
