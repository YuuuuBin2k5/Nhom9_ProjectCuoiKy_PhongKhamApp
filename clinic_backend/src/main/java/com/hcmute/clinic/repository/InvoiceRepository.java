package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.Invoice;
import com.hcmute.clinic.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Invoice> findByPaymentStatus(InvoiceStatus status);
    List<Invoice> findByPatientIdAndPaymentStatus(Long patientId, InvoiceStatus status);
    java.util.Optional<Invoice> findByTreatmentPlanId(Long treatmentPlanId);
}
