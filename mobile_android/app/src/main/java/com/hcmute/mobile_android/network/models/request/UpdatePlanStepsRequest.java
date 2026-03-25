package com.hcmute.mobile_android.network.models.request;

import java.util.List;

public class UpdatePlanStepsRequest {
    private List<StepItem> steps;

    public UpdatePlanStepsRequest(List<StepItem> steps) {
        this.steps = steps;
    }

    public List<StepItem> getSteps() { return steps; }
    public void setSteps(List<StepItem> steps) { this.steps = steps; }

    public static class StepItem {
        private Long serviceId;
        private Long clinicRoomId;
        private Integer sequenceOrder;
        private String toothNumber;
        private String doctorConclusion;
        private String status;

        public StepItem() {}

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public Long getClinicRoomId() { return clinicRoomId; }
        public void setClinicRoomId(Long clinicRoomId) { this.clinicRoomId = clinicRoomId; }

        public Integer getSequenceOrder() { return sequenceOrder; }
        public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

        public String getToothNumber() { return toothNumber; }
        public void setToothNumber(String toothNumber) { this.toothNumber = toothNumber; }

        public String getDoctorConclusion() { return doctorConclusion; }
        public void setDoctorConclusion(String doctorConclusion) { this.doctorConclusion = doctorConclusion; }
    }
}
