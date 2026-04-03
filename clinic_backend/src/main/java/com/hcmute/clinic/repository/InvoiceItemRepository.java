package com.hcmute.clinic.repository;

import com.hcmute.clinic.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý chi tiết các mục trong hóa đơn (InvoiceItem).
 */
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    
    /**
     * Tìm danh sách các mục hóa đơn theo ID hóa đơn.
     * @param invoiceId ID của hóa đơn.
     * @return Danh sách các mục hóa đơn.
     */
    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    /**
     * Kiểm tra xem dịch vụ có đang được sử dụng trong bất kỳ mục hóa đơn nào không.
     * @param serviceId ID của dịch vụ.
     * @return true nếu tồn tại, ngược lại false.
     */
    boolean existsByServiceId(Long serviceId);

    /**
     * Tính toán doanh thu theo danh mục dịch vụ trong khoảng thời gian xác định.
     * @param startDate Ngày bắt đầu.
     * @param endDate Ngày kết thúc.
     * @return Danh sách các đối tượng RevenueCategoryDTO chứa tên danh mục và tổng doanh thu.
     */
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
