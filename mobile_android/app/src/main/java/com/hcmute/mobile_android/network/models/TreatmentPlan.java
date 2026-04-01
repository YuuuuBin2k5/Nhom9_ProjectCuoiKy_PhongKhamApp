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
    private boolean isDraft;
    private Long prescriptionId;
    private String diagnosis;
    private String advice;
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

    public boolean isDraft() { return isDraft; }
    public void setDraft(boolean draft) { isDraft = draft; }

    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }

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
        private String doctorName;
        private String description;
        private Integer stepOrder;
        private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
        private String toothNumber;
        private Double estimatedPrice;
        private Double actualPrice;
        private String doctorConclusion;
        private String createdAt;
        private String completedAt;
        private String roomName;
        private String uiTemplateType;
        private boolean editable;
        private String medicationDetails;
        private Long prescriptionId;
        private String diagnosis;
        private String advice;
        private List<PrescriptionDetail> prescriptionDetails;
        private List<String> imageUrls;

        // Constructors
        public Step() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getMedicationDetails() { return medicationDetails; }
        public void setMedicationDetails(String medicationDetails) { this.medicationDetails = medicationDetails; }

        public Long getPrescriptionId() { return prescriptionId; }
        public String getDiagnosis() { return diagnosis; }
        public String getAdvice() { return advice; }

        public List<PrescriptionDetail> getPrescriptionDetails() { return prescriptionDetails; }
        public void setPrescriptionDetails(List<PrescriptionDetail> prescriptionDetails) { this.prescriptionDetails = prescriptionDetails; }

        public Long getTreatmentPlanId() { return treatmentPlanId; }
        public void setTreatmentPlanId(Long treatmentPlanId) { this.treatmentPlanId = treatmentPlanId; }

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getStepOrder() { return stepOrder; }
        public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getToothNumber() { return toothNumber; }
        public void setToothNumber(String toothNumber) { this.toothNumber = toothNumber; }

        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

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

        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }

        public String getUiTemplateType() { return uiTemplateType; }
        public void setUiTemplateType(String uiTemplateType) { this.uiTemplateType = uiTemplateType; }

        // Helper methods
        public boolean isCompleted() {
            return "COMPLETED".equalsIgnoreCase(status);
        }

        public boolean isInProgress() {
            return "IN_PROGRESS".equalsIgnoreCase(status);
        }

        public boolean isPending() {
            return "PENDING".equals(status);
        }

        public boolean isEditable() { return editable; }
        public void setEditable(boolean editable) { this.editable = editable; }

        public static class PrescriptionDetail {
            private Long id;
            private String medicineName;
            private String dosage;
            private String frequency;
            private String duration;
            private String unit;

            public Long getId() { return id; }
            public String getMedicineName() { return medicineName; }
            public String getDosage() { return dosage; }
            public String getFrequency() { return frequency; }
            public String getDuration() { return duration; }
            public String getUnit() { return unit; }
        }
    }
}