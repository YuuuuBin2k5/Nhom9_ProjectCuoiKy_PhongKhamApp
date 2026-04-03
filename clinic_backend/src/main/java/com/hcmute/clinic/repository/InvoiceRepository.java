package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Invoice;
import com.hcmute.clinic.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository quản lý dữ liệu thực thể Hóa đơn (Invoice).
 * Quản lý quy trình thanh toán và đối soát tài chính (UC_08).
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Invoice> findByPaymentStatus(InvoiceStatus status);
    List<Invoice> findByPatientIdAndPaymentStatus(Long patientId, InvoiceStatus status);
    java.util.Optional<Invoice> findByTreatmentPlanId(Long treatmentPlanId);
    
    @Query("SELECT i FROM Invoice i WHERE i.treatmentPlan.appointment.id = :appointmentId")
    java.util.Optional<Invoice> findByAppointmentId(@Param("appointmentId") Long appointmentId);
    
    List<Invoice> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
