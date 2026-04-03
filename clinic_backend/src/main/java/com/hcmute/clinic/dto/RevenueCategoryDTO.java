package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO chứa thông tin doanh thu theo từng danh mục dịch vụ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueCategoryDTO {
    private String categoryName;
    private BigDecimal totalAmount;
}
