package com.hcmute.mobile_android.network.models;

import java.util.List;

public class TreatmentPlan {
    private Long id;
    private Long patientId;
    private String patientName;
    private String status; // DRAFT, ACTIVE, COMPLETED, CANCELLED
    private String createdAt;
    private String updatedAt;
    private Double totalEstimatedCost;
    private Double totalActualCost;
    private List<Step> steps;

    // Constructors
    public TreatmentPlan() {}

    public TreatmentPlan(Long id, Long patientId, String status) {
        this.id = id;
        this.patientId = patientId;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Double getTotalEstimatedCost() { return totalEstimatedCost; }
    public void setTotalEstimatedCost(Double totalEstimatedCost) { this.totalEstimatedCost = totalEstimatedCost; }

    public Double getTotalActualCost() { return totalActualCost; }
    public void setTotalActualCost(Double totalActualCost) { this.totalActualCost = totalActualCost; }

    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }

    // Helper methods
    public String getStatusDisplay() {
        if (status == null) return "Không xác định";
        switch (status.toUpperCase()) {
            case "DRAFT": return "Bản nháp";
            case "ACTIVE": return "Đang điều trị";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    // Nested class for treatment plan steps
    public static class Step {
        private Long id;
        private Long treatmentPlanId;
        private Long serviceId;
        private String serviceName;
        private String description;
        private Integer stepOrder;
        private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
        private Integer toothNumber;
        private Double estimatedPrice;
        private Double actualPrice;
        private String doctorConclusion;
        private String uiTemplateType; // GENERAL, ORTHO, SURGERY
        private String roomName;
        private String createdAt;
        private String completedAt;

        // Constructors
        public Step() {}

        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }

        // Getters and Setters
        public String getUiTemplateType() { return uiTemplateType; }
        public void setUiTemplateType(String uiTemplateType) { this.uiTemplateType = uiTemplateType; }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTreatmentPlanId() { return treatmentPlanId; }
        public void setTreatmentPlanId(Long treatmentPlanId) { this.treatmentPlanId = treatmentPlanId; }

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getStepOrder() { return stepOrder; }
        public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getToothNumber() { return toothNumber; }
        public void setToothNumber(Integer toothNumber) { this.toothNumber = toothNumber; }

        public Double getEstimatedPrice() { return estimatedPrice; }
        public void setEstimatedPrice(Double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

        public Double getActualPrice() { return actualPrice; }
        public void setActualPrice(Double actualPrice) { this.actualPrice = actualPrice; }

        public String getDoctorConclusion() { return doctorConclusion; }
        public void setDoctorConclusion(String doctorConclusion) { this.doctorConclusion = doctorConclusion; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getCompletedAt() { return completedAt; }
        public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

        // Helper methods
        public boolean isCompleted() {
            return "COMPLETED".equals(status);
        }

        public boolean isInProgress() {
            return "IN_PROGRESS".equals(status);
        }

        public boolean isPending() {
            return "PENDING".equals(status);
        }
    }
}