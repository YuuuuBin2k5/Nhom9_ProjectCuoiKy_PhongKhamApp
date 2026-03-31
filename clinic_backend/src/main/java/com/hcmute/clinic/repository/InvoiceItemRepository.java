package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    
    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    boolean existsByServiceId(Long serviceId);

    @org.springframework.data.jpa.repository.Query("SELECT new com.hcmute.clinic.dto.RevenueCategoryDTO(c.name, SUM(ii.totalPrice)) " +
           "FROM InvoiceItem ii " +
           "JOIN ii.invoice i " +
           "JOIN ii.service s " +
           "JOIN s.category c " +
           "WHERE i.createdAt >= :startDate AND i.createdAt <= :endDate " +
           "GROUP BY c.name")
    List<com.hcmute.clinic.dto.RevenueCategoryDTO> calculateRevenueByCategory(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}
