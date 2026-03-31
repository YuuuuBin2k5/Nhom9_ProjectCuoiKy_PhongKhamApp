package com.hcmute.mobile_android.network.models;

import java.util.List;

public class TreatmentTemplate {
    private Long id;
    private String name;
    private String description;
    private String uiTemplateType; // GENERAL, SURGERY, ORTHO, IMPLANT, PERIO
    private Boolean isActive;
    private List<TemplateStep> steps;

    // Constructors
    public TreatmentTemplate() {}

    public TreatmentTemplate(Long id, String name, String description, String uiTemplateType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.uiTemplateType = uiTemplateType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUiTemplateType() { return uiTemplateType; }
    public void setUiTemplateType(String uiTemplateType) { this.uiTemplateType = uiTemplateType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public List<TemplateStep> getSteps() { return steps; }
    public void setSteps(List<TemplateStep> steps) { this.steps = steps; }

    // Nested class for template steps
    public static class TemplateStep {
        private Long id;
        private String serviceName;
        private String description;
        private Integer stepOrder;
        private Double estimatedPrice;
        private Integer estimatedDurationMinutes;
        private String medicationDetails;

        // Constructors
        public TemplateStep() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getMedicationDetails() { return medicationDetails; }
        public void setMedicationDetails(String medicationDetails) { this.medicationDetails = medicationDetails; }

        public Integer getStepOrder() { return stepOrder; }
        public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

        public Double getEstimatedPrice() { return estimatedPrice; }
        public void setEstimatedPrice(Double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

        public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
        public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    }
}