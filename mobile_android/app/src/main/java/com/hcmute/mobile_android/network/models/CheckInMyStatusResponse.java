package com.hcmute.mobile_android.network.models;

public class CheckInMyStatusResponse {
    private boolean checkedIn;
    private Integer queueNumber;
    private Integer queuePosition;
    private Integer estimatedWaitTime;
    private String roomName;
    private String roomLocation;
    private String status;
    private String statusLabel;
    private String hint;
    
    // Queue estimation fields (new)
    private String estimateDisplayType;
    private Integer estimatedMinutes;
    private Integer minMinutes;
    private Integer maxMinutes;
    private String estimateMessage;
    private String estimateConfidence;
    private Boolean showApproximateLabel;
    private String estimateTitle;
    private String estimateSubtitle;
    private Integer countdownStartSeconds; // For countdown timer (position 1 only)
    
    // Treatment context fields (Phase 3 enhancement)
    private String doctorName;
    private String serviceName;
    private Long treatmentPlanId;
    private String currentStepName;
    private Integer currentStepNumber;
    private Integer totalSteps;

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomLocation() {
        return roomLocation;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public String getHint() {
        return hint;
    }
    
    // Getters for estimate fields
    public String getEstimateDisplayType() { return estimateDisplayType; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public Integer getMinMinutes() { return minMinutes; }
    public Integer getMaxMinutes() { return maxMinutes; }
    public String getEstimateMessage() { return estimateMessage; }
    public String getEstimateConfidence() { return estimateConfidence; }
    public Boolean getShowApproximateLabel() { return showApproximateLabel; }
    public String getEstimateTitle() { return estimateTitle; }
    public String getEstimateSubtitle() { return estimateSubtitle; }
    public Integer getCountdownStartSeconds() { return countdownStartSeconds; }
    
    // Getters for treatment context fields
    public String getDoctorName() { return doctorName; }
    public String getServiceName() { return serviceName; }
    public Long getTreatmentPlanId() { return treatmentPlanId; }
    public String getCurrentStepName() { return currentStepName; }
    public Integer getCurrentStepNumber() { return currentStepNumber; }
    public Integer getTotalSteps() { return totalSteps; }
}
