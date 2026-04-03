package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO Phản hồi khi thêm một dịch vụ nha khoa tổng quát vào phác đồ.
 * Chứa thông tin về bước điều trị vừa tạo và tổng chi phí cập nhật.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralServiceResponse {
    private Long stepId;              // ID of the created TreatmentPlanStep
    private String serviceName;       // Name of the service
    private BigDecimal price;         // Price of the service
    private BigDecimal totalPlanCost; // Updated total cost of the treatment plan
}
