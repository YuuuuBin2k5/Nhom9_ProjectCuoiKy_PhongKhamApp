package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý hàng đợi Check-in (CheckInQueue).
 * Hỗ trợ điều phối thứ tự khám của bệnh nhân tại các phòng chức năng (UC_06).
 */
@Repository
public interface CheckInQueueRepository extends JpaRepository<CheckInQueue, Long> {

    Optional<CheckInQueue> findByAppointmentId(Long appointmentId);

    List<CheckInQueue> findByStatusInAndCheckInTimeBetween(
            List<QueueStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<CheckInQueue> findByClinicRoomIdAndStatusInAndCheckInTimeBetweenOrderByPriorityLevelDescQueueNumberAsc(
            Long clinicRoomId, List<QueueStatus> statuses, LocalDateTime start, LocalDateTime end);

    long countByClinicRoomIdAndStatusAndCheckInTimeBetween(
            Long clinicRoomId, QueueStatus status, LocalDateTime start, LocalDateTime end);

    default List<CheckInQueue> findTodayByClinicRoomId(Long clinicRoomId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        return findByRoomAndDateRange(
                clinicRoomId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay(),
                List.of(QueueStatus.WAITING, QueueStatus.IN_PROGRESS, QueueStatus.PAUSED_FOR_TEST, QueueStatus.RETURNED_PRIORITY));
    }

    @Query("""
            SELECT q FROM CheckInQueue q
            JOIN FETCH q.appointment a
            JOIN FETCH a.patient
            JOIN FETCH q.clinicRoom
            WHERE q.clinicRoom.id = :roomId
            AND q.checkInTime >= :start
            AND q.checkInTime < :end
            AND q.status IN :statuses
            ORDER BY q.priorityLevel DESC, q.queueNumber ASC
            """)
    List<CheckInQueue> findByRoomAndDateRange(
            @Param("roomId") Long roomId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<QueueStatus> statuses);

    @Query("""
            SELECT q FROM CheckInQueue q
            JOIN FETCH q.appointment a
            JOIN FETCH a.patient
            JOIN FETCH q.clinicRoom
            WHERE a.patient.id = :patientId
            AND q.checkInTime >= :start
            AND q.checkInTime < :end
            ORDER BY q.checkInTime DESC
            """)
    List<CheckInQueue> findTodayForPatient(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT q FROM CheckInQueue q
            JOIN FETCH q.appointment a
            JOIN FETCH a.patient
            JOIN FETCH q.clinicRoom
            WHERE q.originalRoomId = :roomId
            AND q.clinicRoom.id != :roomId
            AND q.checkInTime >= :start
            AND q.checkInTime < :end
            AND q.status IN :statuses
            ORDER BY q.priorityLevel DESC, q.queueNumber ASC
            """)
    List<CheckInQueue> findTransferredByRoomAndDateRange(
            @Param("roomId") Long roomId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<QueueStatus> statuses);
}
