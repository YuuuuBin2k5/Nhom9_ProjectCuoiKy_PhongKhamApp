package com.hcmute.mobile_android.network.models;

import java.util.List;

public class AdminTreatmentPlanTemplateRequest {
    private String name;
    private String description;
    private boolean active;
    private List<StepRequest> steps;

    public static class StepRequest {
        private Long serviceId;
        private Long clinicRoomId;
        private Integer sequenceOrder;
        private String medicationDetails;

        public StepRequest() {}

        public StepRequest(Long serviceId, Long clinicRoomId, Integer sequenceOrder, String medicationDetails) {
            this.serviceId = serviceId;
            this.clinicRoomId = clinicRoomId;
            this.sequenceOrder = sequenceOrder;
            this.medicationDetails = medicationDetails;
        }

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public Long getClinicRoomId() { return clinicRoomId; }
        public void setClinicRoomId(Long clinicRoomId) { this.clinicRoomId = clinicRoomId; }

        public Integer getSequenceOrder() { return sequenceOrder; }
        public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

        public String getMedicationDetails() { return medicationDetails; }
        public void setMedicationDetails(String medicationDetails) { this.medicationDetails = medicationDetails; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<StepRequest> getSteps() { return steps; }
    public void setSteps(List<StepRequest> steps) { this.steps = steps; }
}
