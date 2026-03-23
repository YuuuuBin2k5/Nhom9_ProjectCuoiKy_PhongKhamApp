package com.hcmute.mobile_android.network.models;

public class CreateTreatmentPlanRequest {
    private Long templateId;
    private Long patientId;

    // Constructors
    public CreateTreatmentPlanRequest() {}

    public CreateTreatmentPlanRequest(Long templateId, Long patientId) {
        this.templateId = templateId;
        this.patientId = patientId;
    }

    // Getters and Setters
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}