package com.hcmute.mobile_android.network.models;

public class QueueItem {
    private Long id;
    private String patientName;
    private String patientPhone;
    private Integer queueNumber;
    private String status; // WAITING, IN_PROGRESS, PAUSED_FOR_TEST, RETURNED_PRIORITY, COMPLETED
    private String serviceName;
    private String appointmentTime;
    private Integer priority; // 0 = normal, 1 = priority (after X-Ray)
    private String roomName;
    private Long appointmentId;
    private Long treatmentPlanStepId;
    private Long patientId;

    // Constructors
    public QueueItem() {}

    public QueueItem(Long id, String patientName, String patientPhone, Integer queueNumber, 
                     String status, String serviceName, String appointmentTime, Integer priority) {
        this.id = id;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.queueNumber = queueNumber;
        this.status = status;
        this.serviceName = serviceName;
        this.appointmentTime = appointmentTime;
        this.priority = priority;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public Integer getQueueNumber() { return queueNumber; }
    public void setQueueNumber(Integer queueNumber) { this.queueNumber = queueNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getTreatmentPlanStepId() { return treatmentPlanStepId; }
    public void setTreatmentPlanStepId(Long treatmentPlanStepId) { this.treatmentPlanStepId = treatmentPlanStepId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    // Helper methods
    public boolean isPriority() {
        return priority != null && priority > 0;
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    public boolean isWaiting() {
        return "WAITING".equals(status);
    }

    public boolean isPausedForTest() {
        return "PAUSED_FOR_TEST".equals(status);
    }

    public boolean isReturnedPriority() {
        return "RETURNED_PRIORITY".equals(status);
    }

    public String getStatusDisplayText() {
        switch (status) {
            case "WAITING": return "Đang chờ";
            case "IN_PROGRESS": return "Đang khám";
            case "PAUSED_FOR_TEST": return "Đi chụp X-Quang";
            case "RETURNED_PRIORITY": return "Ưu tiên (đã chụp)";
            case "COMPLETED": return "Hoàn thành";
            case "SKIPPED": return "Bỏ qua";
            default: return status;
        }
    }
}