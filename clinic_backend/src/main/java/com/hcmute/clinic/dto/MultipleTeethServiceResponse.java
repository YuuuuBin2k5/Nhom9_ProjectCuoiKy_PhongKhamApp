package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO Phản hồi khi thêm dịch vụ cho nhiều răng cùng lúc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipleTeethServiceResponse {
    private List<StepInfo> createdSteps;
    private BigDecimal totalPlanCost;
    private String message;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepInfo {
        private Long stepId;
        private String toothNumber;
        private String serviceName;
        private BigDecimal price;
    }
}
