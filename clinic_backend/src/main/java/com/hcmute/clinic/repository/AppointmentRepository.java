package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
            Long patientId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    default List<Appointment> findTodayByPatientId(Long patientId) {
        LocalDate today = LocalDate.now();
        return findByPatientIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                patientId,
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
}
