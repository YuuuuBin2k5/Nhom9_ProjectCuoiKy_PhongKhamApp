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

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
            Long patientId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    boolean existsByDoctorIdAndAppointmentDatetimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    boolean existsByPatientIdAndStatusIn(Long patientId, List<AppointmentStatus> statuses);

    default List<Appointment> findTodayByPatientId(Long patientId) {
        LocalDate today = LocalDate.now();
        return findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                patientId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
    }
    
    List<Appointment> findByDoctorIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
            Long doctorId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    default List<Appointment> findTodayByDoctorId(Long doctorId) {
        LocalDate today = LocalDate.now();
        return findByDoctorIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                doctorId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
    }

    default List<Appointment> findUpcomingByPatientId(Long patientId, int daysAhead) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDate.now().plusDays(daysAhead).atTime(23, 59, 59);
        return findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                patientId, from, to);
    }

    Optional<Appointment> findFirstByPatientIdAndStatusInOrderByAppointmentDatetimeDesc(
            Long patientId, List<AppointmentStatus> statuses);

    long countByDoctorId(Long doctorId);
    
    List<Appointment> findByAppointmentDatetimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Appointment> findByStatusAndAppointmentDatetimeBetween(
        AppointmentStatus status, LocalDateTime start, LocalDateTime end);
    
    // Search by date
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDatetime) = :date")
    List<Appointment> findByDate(@Param("date") LocalDate date);
}
