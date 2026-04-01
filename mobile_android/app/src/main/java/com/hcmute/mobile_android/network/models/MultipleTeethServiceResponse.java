package com.hcmute.mobile_android.network.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response model for bulk teeth service addition
 */
public class MultipleTeethServiceResponse {
    private List<StepInfo> createdSteps;
    private BigDecimal totalPlanCost;
    private String message;

    public static class StepInfo {
        private Long stepId;
        private String toothNumber;
        private String serviceName;
        private BigDecimal price;

        public StepInfo() {
        }

        public StepInfo(Long stepId, String toothNumber, String serviceName, BigDecimal price) {
            this.stepId = stepId;
            this.toothNumber = toothNumber;
            this.serviceName = serviceName;
            this.price = price;
        }

        public Long getStepId() {
            return stepId;
        }

        public void setStepId(Long stepId) {
            this.stepId = stepId;
        }

        public String getToothNumber() {
            return toothNumber;
        }

        public void setToothNumber(String toothNumber) {
            this.toothNumber = toothNumber;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    public MultipleTeethServiceResponse() {
    }

    public MultipleTeethServiceResponse(List<StepInfo> createdSteps, BigDecimal totalPlanCost, String message) {
        this.createdSteps = createdSteps;
        this.totalPlanCost = totalPlanCost;
        this.message = message;
    }

    public List<StepInfo> getCreatedSteps() {
        return createdSteps;
    }

    public void setCreatedSteps(List<StepInfo> createdSteps) {
        this.createdSteps = createdSteps;
    }

    public BigDecimal getTotalPlanCost() {
        return totalPlanCost;
    }

    public void setTotalPlanCost(BigDecimal totalPlanCost) {
        this.totalPlanCost = totalPlanCost;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
